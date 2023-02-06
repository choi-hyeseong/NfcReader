package com.comet.nfcreader

import android.content.SharedPreferences
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

//AID = (F239856324897348, hex code (16진수, 짝수로 끝나야함.)
val cardAID = byteArrayOf(0xF2.toByte(),
                          0x39.toByte(),
                          0x85.toByte(),
                          0x63.toByte(),
                          0x24.toByte(),
                          0x89.toByte(),
                          0x73.toByte(),
                          0x48.toByte())

const val SUPPORT_NFC_TYPE =
    NfcAdapter.FLAG_READER_NFC_A.or(NfcAdapter.FLAG_READER_NFC_B).or(NfcAdapter.FLAG_READER_NFC_F)
        .or(NfcAdapter.FLAG_READER_NFC_V).or(NfcAdapter.FLAG_READER_NFC_BARCODE)
        .or(NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS) // or expression (비트 연산자 사용하여 지원하는 타입 명시.)
const val TAG = "NFC_READER" //debug tag
const val DELAY = 150 //NFC Reading period
const val SERVER_IP_TAG = "SERVER_IP"
const val TIMEOUT = 2L

class MainActivity : AppCompatActivity(), NfcAdapter.ReaderCallback {

    private lateinit var adapter : NfcAdapter
    private lateinit var edit : EditText
    private val client = OkHttpClient.Builder().connectTimeout(TIMEOUT, TimeUnit.SECONDS)
    .readTimeout(TIMEOUT, TimeUnit.SECONDS).build()
    private val preferences : SharedPreferences by lazy {
        applicationContext.getSharedPreferences(TAG, MODE_PRIVATE) //non root
    }

    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //ip 저장
        findViewById<Button>(R.id.button).apply { setOnClickListener { saveData() } } //ip 저장
        //불러오기
        edit = findViewById<EditText?>(R.id.serverIP).apply { setText(getData()) }
    }

    //현재 앱이 실행중인 경우.
    override fun onResume() {
        super.onResume()
        Log.i(TAG, getData())
        //nfc 태그 리딩 딜레이. (1000 = 1s?)
        val options = Bundle().apply { putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, DELAY) }
        adapter = NfcAdapter.getDefaultAdapter(this)
            .also { it.enableReaderMode(this, this, SUPPORT_NFC_TYPE, options) }
    }

    //앱이 상호작용 불가능한 경우 (백그라운드 등등...)
    override fun onPause() {
        saveData()
        super.onPause()
        adapter.disableReaderMode(this)
    }

    //be called when NFC Tagging
    override fun onTagDiscovered(tag : Tag?) {
        //when tag isn't null.
        tag?.let { it ->
            Log.i(TAG, "tag read.")
            // TODO 비 안드로이드 계열 nfc 태그 처리 로직 구현부.
            // for android
            IsoDep.get(it)?.run {
                try {
                    connect() // 리턴값 필요없으니 람다 변수 충돌 방지위한 run
                    val result =
                        transceive(buildAPDU(cardAID)) //단순한 apdu 명령 요청 및 nfc 태그(클라이언트) 의 응답 바이트. (APDU 형식)
                    //send response apdu 메소드로 보낸값도 읽을 수 있음. 그전에는 빈 데이터만
                    if (result == null || result.size < 3)
                        return // TODO error handling (beep...)
                    else {
                        //apdu 명령어를 제외한 데이터 긁어오기. (여기선 string을 byte로 직렬화한 데이터)
                        val strData = String(result.sliceArray(5..result.lastIndex)) //toString 사용시 역직렬화 안됨..!
                         Thread {
                            val ip = edit.text
                            //127.0.0.1:8080/mdm/request
                            val request = Request.Builder().url("$ip/mdm/request").header("Content-Type", "application/json").post(JSONObject().put("data", strData).toString().toRequestBody()).build()
                            val response = client.newCall(request).execute() //thread
                            Log.i(TAG, strData)
                             if (!response.isSuccessful)
                                 Log.w(TAG, "ERROR!")// TODO error handle, 에러타입 여러게 enum으로 명시하기
                            else {
                                if (response.code != RESULT_OK) {
                                    Log.i(TAG, "SUCCESS")
                                }
                                else {
                                    Log.w(TAG, "ERROR") //error handle
                                }
                            }

                        }.apply { start() }
                        // strData = (Encrypt[UUID-TIME])|Android
                    }
                }
                catch (e : Exception) {
                    e.localizedMessage?.let { Log.e(TAG, it) }
                    close() //더이상 처리할 필요 X
                }
                finally {

                }
            }
        }


    }

    //APDU 구성 (with data(aid))
    private fun buildAPDU(data : ByteArray) : ByteArray {
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

    private fun saveData() {
        preferences.edit().putString(
            SERVER_IP_TAG, edit.text.toString()).commit()
    }

    private fun getData() : String {
        return preferences.getString(SERVER_IP_TAG, "")!!
    }
}