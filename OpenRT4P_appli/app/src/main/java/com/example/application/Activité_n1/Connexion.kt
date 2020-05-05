package com.example.application.Activité_n1


import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.application.Activité_n1.Bluetooth.Peripherique
import com.example.application.Activité_n1.Bluetooth.PeripheriqueAdapter
import com.example.application.Activité_n1.RecyclerView.RecyclerTouch
import com.example.application.Activité_n2.MainActivity
import com.example.application.R

/**
 * Première activité lancé par l'application ayant pour but de choisir le bon périphérique parmi une liste de périphériques connus appareillé en bluetooth
 * Lorsque l'on click sur le bon périphérique, il s'affiche au dessus du bouton 'connecter' et on peut appuyer ainsi sur ce bouton pour
 * aller à l'activité suivante.
 **/

class Connexion : AppCompatActivity() {

    private var devices: Set<BluetoothDevice>? = null
    private var adaptateurBluetooth: BluetoothAdapter? = null
    private val bluetoothReceiver: BroadcastReceiver? = null
    private var peripheriques: ArrayList<Peripherique>? = ArrayList()
    private var peripherique: Peripherique? = null
    private var noms: ArrayList<String>? = null
    private val handler: Handler? = null

    private var btnConnecter: Button? = null
    private var btnRefresh: ImageButton? = null
    private var listePeripheriques: androidx.recyclerview.widget.RecyclerView? = null

    private var peripheriqueText: TextView? = null
    private var progressBar: ProgressBar? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.connexion)
        context = applicationContext

        peripheriqueText = findViewById(R.id.textPeripheriqueName)
        btnConnecter = findViewById(R.id.btnConnecter)
        btnRefresh = findViewById(R.id.btnRefresh)
        progressBar = findViewById(R.id.progressBar_cyclic)
        progressBar!!.indeterminateDrawable.setColorFilter(0xFFFF0000.toInt(), android.graphics.PorterDuff.Mode.MULTIPLY) //Change la coleur de la progress bar

        connectBluetooth()

        btnRefresh!!.setOnClickListener {
            peripheriques!!.clear()
            if (adaptateurBluetooth!!.isEnabled) {
                showPeripherals()
            } else {
                connectBluetooth()
            }
        }

        btnConnecter!!.setOnClickListener {

            if (peripheriqueText!!.text.toString() == "Appareil à connecter") run {
                Toast.makeText(applicationContext, "Please choose a peripherique.", Toast.LENGTH_SHORT).show()
            } else if (!adaptateurBluetooth!!.isEnabled) {
                connectBluetooth()
            } else {
                val str = "Trying to connect to " + peripheriqueText!!.text + "..."
                Toast.makeText(applicationContext, str, Toast.LENGTH_SHORT).show()
                progressBar!!.visibility = View.VISIBLE
                peripherique!!.connecter()

                if (!peripherique!!.isConnected) {
                    Toast.makeText(applicationContext, "Something went wrong, connexion impossible, please try again.", Toast.LENGTH_SHORT).show()
                    progressBar!!.visibility = View.INVISIBLE
                } else {
                    Peripherique.peripherique = peripherique
                    val intent = Intent(this@Connexion, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }

        }
    }

    /**
     * Cette fonction vérifie si le bluetooh est activé. Si il ne l'est pas une demande d'activation est faite.
     * Par la suite on déclenche l'affichage des périphériques quand le bluetooh est activé.
     */
    private fun connectBluetooth() {
        adaptateurBluetooth = BluetoothAdapter.getDefaultAdapter()
        if (adaptateurBluetooth == null) {
            Toast.makeText(applicationContext, "Il se peut que votre smarthphone ne supporte pas le bluetooth!", Toast.LENGTH_SHORT).show()
        } else {
            if (!adaptateurBluetooth!!.isEnabled) {
                //  Toast.makeText(applicationContext, "Bluetooth non activé !", Toast.LENGTH_SHORT).show()
                val activeBlueTooth = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(activeBlueTooth, REQUEST_CODE_ENABLE_BLUETOOTH)
                //bluetoothAdapter.enable();
            } else {
                Toast.makeText(applicationContext, "Bluetooth activé", Toast.LENGTH_SHORT).show()
                showPeripherals()
            }
        }
    }


    /**
     * Cette fonction permet d'afficher les périphériques bluetooth connus et appareillés sur la page.
     * Mais aussi de choisir sur lequel se connecter.
     */

    private fun showPeripherals() {
        // Recherche des périphériques connus
        peripheriques!!.clear()
        noms = ArrayList()
        devices = adaptateurBluetooth!!.bondedDevices
        for (blueDevice in devices!!) {
            //Toast.makeText(getApplicationContext(), "Périphérique = " + blueDevice.getName(), Toast.LENGTH_SHORT).show();
            peripheriques!!.add(Peripherique(blueDevice, handler))
            noms!!.add(blueDevice.name)
        }

        if (peripheriques!!.size == 0)
            peripheriques!!.add(Peripherique(null, handler))
        if (noms!!.size == 0)
            noms!!.add("Aucun")


        listePeripheriques = findViewById<View>(R.id.bluetoothList) as androidx.recyclerview.widget.RecyclerView
        val layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        listePeripheriques!!.layoutManager = layoutManager
        listePeripheriques!!.itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator()
        val peripheriqueAdapter = PeripheriqueAdapter(this, peripheriques!!)
        listePeripheriques!!.adapter = peripheriqueAdapter

        listePeripheriques!!.addOnItemTouchListener(RecyclerTouch(this, listePeripheriques!!, object : RecyclerTouch.ClickListener {
            override fun onClick(view: View?, position: Int) {
                peripherique = peripheriques!![position]
                peripheriqueText!!.text = peripherique!!.nom
            }

            override fun onLongClick(view: View?, position: Int) {
                peripherique = peripheriques!![position]
                peripheriqueText!!.text = peripherique!!.nom
            }

        }))
    }

    /*
    Cette fonction permet d'afficher une notification lorsque le bluetooth du téléphone est activé ou non
     */

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_CODE_ENABLE_BLUETOOTH)
            return
        if (resultCode == Activity.RESULT_OK) {
            Toast.makeText(context, "Bluetooth activé", Toast.LENGTH_SHORT).show()
            finish()
            startActivity(this.intent)
        } else {
            Toast.makeText(context, "Bluetooth non activé !", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        if (adaptateurBluetooth != null) {
            adaptateurBluetooth!!.cancelDiscovery()
        }
        if (bluetoothReceiver != null) {
            unregisterReceiver(bluetoothReceiver)
        }
        super.onDestroy()

    }

    companion object {
        private val REQUEST_CODE_ENABLE_BLUETOOTH = 0
        private var context: Context? = null
    }
}

