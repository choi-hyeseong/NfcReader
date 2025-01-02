package com.comet.nfcreader

import android.media.AudioManager
import android.media.ToneGenerator
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.comet.nfcreader.databinding.ActivityMainBinding
import com.comet.nfcreader.reader.server.type.ResponseStatus
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NfcAdapter.ReaderCallback {

    companion object {

        //AID = (F239856324897348, hex code (16진수, 짝수로 끝나야함.)
        private val cardAID = byteArrayOf(
            0xF2.toByte(),
            0x39.toByte(),
            0x85.toByte(),
            0x63.toByte(),
            0x24.toByte(),
            0x89.toByte(),
            0x73.toByte(),
            0x48.toByte()
        )

        private const val SUPPORT_NFC_TYPE =
            NfcAdapter.FLAG_READER_NFC_A.or(NfcAdapter.FLAG_READER_NFC_B)
                .or(NfcAdapter.FLAG_READER_NFC_F)
                .or(NfcAdapter.FLAG_READER_NFC_V).or(NfcAdapter.FLAG_READER_NFC_BARCODE)
                .or(NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS) // or expression (비트 연산자 사용하여 지원하는 타입 명시.)
        private const val DELAY = 150 //NFC Reading period
    }

    // NFC Adapter. 미지원 기기를 위해 nullable로 받음
    private val adapter: NfcAdapter? by lazy { NfcAdapter.getDefaultAdapter(this) }
    private val viewModel: MainViewModel by viewModels()
    private val tone: ToneGenerator by lazy { ToneGenerator(AudioManager.STREAM_MUSIC, 100) } //삑소리


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = ActivityMainBinding.inflate(layoutInflater)
        setContentView(view.root)

        initView(view)
        initObserver(view)
    }

    private fun initView(bind: ActivityMainBinding) {
        //ip 저장
        bind.button.setOnClickListener {
            viewModel.saveServerIP(bind.serverIP.text.toString())
            Toast.makeText(this, R.string.server_load_complete, Toast.LENGTH_SHORT).show()
        }
    }

    private fun initObserver(bind: ActivityMainBinding) {
        viewModel.serverIPLiveData.observe(this) {
            bind.serverIP.setText(it)
        }

        viewModel.responseLiveData.observe(this) { status ->
            when (status) {
                ResponseStatus.OK -> playBeep()
                ResponseStatus.ERROR -> playErrorBeep()
                ResponseStatus.DATA_NOT_INIT -> {
                    playErrorBeep()
                    Toast.makeText(this, R.string.serverip_not_initialized, Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    //현재 앱이 실행중인 경우.
    override fun onStart() {
        super.onStart()
        startNFCListening()
    }

    //앱이 상호작용 불가능한 경우 (백그라운드 등등...)
    override fun onStop() {
        super.onStop()
        stopNFCListening()
    }

    // nfc 읽기 시작
    private fun startNFCListening() {
        // 종료
        if (adapter == null) {
            Toast.makeText(this, R.string.nfc_not_support, Toast.LENGTH_SHORT).show()
            finish()
        }
        val options = Bundle().apply { putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, DELAY) }
        adapter?.enableReaderMode(this, this, SUPPORT_NFC_TYPE, options)
    }

    // nfc 읽기 종료
    private fun stopNFCListening() {
        adapter?.disableReaderMode(this)
    }

    //be called when NFC Tagging
    // repository로 옮기려 했으나, activity에서 수행되어야 한다고함
    override fun onTagDiscovered(tag: Tag) {
        //when tag isn't null.
        val iso: IsoDep = IsoDep.get(tag) ?: return   // TODO 비 안드로이드 계열 nfc 태그 처리 로직 구현부.
        Log.i(getClassName(), "tag read.")

        kotlin.runCatching {
            iso.connect()
            //send response apdu 메소드로 보낸값도 읽을 수 있음. 그전에는 빈 데이터만
            val response =
                iso.transceive(buildAPDU(cardAID)) //단순한 apdu 명령 요청 및 nfc 태그(클라이언트) 의 응답 바이트. (APDU 형식)
            if (response == null || response.size < 3) {
                playErrorBeep() //실패시
                return
            }

            // body = (Encrypt[UUID-TIME])|Android
            val body =
                String(response.sliceArray(5..response.lastIndex)) //toString 사용시 역직렬화 안됨..! < 짜르기
            viewModel.requestTagging(body)
        }.onFailure {
            // 실패시 error
            Log.e(getClassName(), it.message, it)
            playErrorBeep()

        }
        iso.close() //더이상 처리할 필요 X
    }

    //APDU 구성 (with data(aid))
    private fun buildAPDU(data: ByteArray): ByteArray {
        val commandApdu = ByteArray(6 + data.size)
        //apdu 구성 00A40400 + 데이터 + LE
        commandApdu[0] = 0x00.toByte() // CLA
        commandApdu[1] = 0xA4.toByte() // INS
        commandApdu[2] = 0x04.toByte() // P1
        commandApdu[3] = 0x00.toByte() // P2
        commandApdu[4] = (data.size and 0x0FF).toByte() // Lc
        System.arraycopy(data, 0, commandApdu, 5, data.size)
        commandApdu[commandApdu.size - 1] = 0x00.toByte() // Le
        return commandApdu
    }


    private fun playBeep() {
        tone.startTone(ToneGenerator.TONE_CDMA_PIP, 150)
    }

    private fun playErrorBeep() {
        tone.startTone(ToneGenerator.TONE_SUP_INTERCEPT, 150)
    }

}

// for logging
fun Any.getClassName(): String = this.javaClass.simpleName
