package com.example.myapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import com.pro100svitlo.creditCardNfcReader.CardNfcAsyncTask
import com.pro100svitlo.creditCardNfcReader.utils.CardNfcUtils

class MainActivity : AppCompatActivity(), CardNfcAsyncTask.CardNfcInterface {

    private var nfcAdapter: NfcAdapter? = null
    private var cardNfcUtils: CardNfcUtils? = null
    private var cardNfcAsyncTask: CardNfcAsyncTask? = null
    private val nfcStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == NfcAdapter.ACTION_ADAPTER_STATE_CHANGED) {
                when (intent.getIntExtra(NfcAdapter.EXTRA_ADAPTER_STATE, NfcAdapter.STATE_OFF)) {
                    NfcAdapter.STATE_OFF -> {
                        Log.d("NFCReader", "User changed NFC state to disabled")
                        changeNfcDispatchState()
                    }
                    NfcAdapter.STATE_ON -> {
                        Log.d("NFCReader", "User changed NFC state to enabled")
                        changeNfcDispatchState()
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        registerReceiver(nfcStateReceiver, IntentFilter(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED))
        initNfcAdapter()
    }

    override fun onResume() {
        super.onResume()
        changeNfcDispatchState()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        nfcAdapter?.let {
            if (!it.isEnabled) return@let

            try {
                cardNfcAsyncTask = CardNfcAsyncTask.Builder(this, intent, false).build()
            } catch (e: Exception) {
                // Issue with trying to init nfc listener
            }
        }
    }

    private fun changeNfcDispatchState() {
        initNfcAdapter()
        nfcAdapter?.let {
            if (it.isEnabled) {
                cardNfcUtils?.enableDispatch()
                Log.d("NFCReader", "NFC is currently enabled")
            } else {
                cardNfcUtils?.disableDispatch()
                Log.d("NFCReader", "NFC is currently disabled")
            }
        }
    }

    private fun initNfcAdapter() {
        if (nfcAdapter != null) return
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        nfcAdapter?.let {
            Log.d("NFCReader", "Device is equipped with NFC scanner")
            cardNfcUtils = CardNfcUtils(this)
        } ?: run {
            Log.d("NFCReader", "Device is not equipped with NFC scanner")
        }
    }

    // NFC lib interface methods
    override fun startNfcReadCard() {
        Log.d("NFCReader", "Preparing to read card startNfcReadCard()")
    }

    override fun cardIsReadyToRead() {
        Log.d("NFCReader", "Card is ready to read cardIsReadyToRead()")
    }

    override fun finishNfcReadCard() {
        Log.d("NFCReader", "Finished reading card successfully finishNfcReadCard()")
        Log.d("NFCReader", "Number: " + cardNfcAsyncTask?.cardNumber +
                " ; Exp date: " + cardNfcAsyncTask?.cardExpireDate)
    }

    override fun cardWithLockedNfc() {
        Log.d("NFCReader", "Scanned card is with locked NFC cardWithLockedNfc()")
    }

    override fun doNotMoveCardSoFast() {
        Log.d("NFCReader", "Card moved too quickly while trying to read it doNotMoveCardSoFast()")
    }

    override fun unknownEmvCard() {
        Log.d("NFCReader", "Tried to read unknown card unknownEmvCard()")
    }
}