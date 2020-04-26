package com.ryanjames.swabergersmobilepos.activity

import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProviders
import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.core.BaseActivity
import com.ryanjames.swabergersmobilepos.core.SwabergersApplication
import com.ryanjames.swabergersmobilepos.database.entity.ModifierInfoEntity
import com.ryanjames.swabergersmobilepos.network.responses.LoginResponse
import com.ryanjames.swabergersmobilepos.network.responses.ModifierInfoResponse
import com.ryanjames.swabergersmobilepos.viewmodels.ModifierInfoViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.net.SocketTimeoutException
import javax.inject.Inject

class MainActivity : BaseActivity() {

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    private lateinit var modifierInfoViewModel: ModifierInfoViewModel
    private var modifierInfos: List<ModifierInfoEntity> = ArrayList()
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private var remoteModifierInfos: List<ModifierInfoResponse> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        SwabergersApplication.appComponent.inject(this)

        modifierInfoViewModel = ViewModelProviders.of(this, viewModelFactory { ModifierInfoViewModel(sharedPreferences) }).get(ModifierInfoViewModel::class.java)

        subscribe()

    }

    private fun subscribe() {
        compositeDisposable.add(
            modifierInfoViewModel.getModifierInfosObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { modifierInfoList ->
                    this.modifierInfos = modifierInfoList
//                    updateModifierListText()
                }
        )

        compositeDisposable.add(
            modifierInfoViewModel.insertErrorObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { error ->
                    error.printStackTrace()
                    showToastMessageShort("Error inserting")
                }
        )


    }

    fun showToastMessageShort(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun onAdd(view: View) {
//        val modifierId = etModifierId.text.toString()
//        val modifierName = etModifierName.text.toString()
//        val priceDelta = etPriceDelta.text.toString().toFloat()
//        val receiptText = etReceiptText.text.toString()
//
//        val modifierInfoEntity = ModifierInfoEntity(modifierId, modifierName, priceDelta, receiptText)
//        modifierInfoViewModel.insert(modifierInfoEntity)

    }

    fun onClickAuthButton(view: View) {
//        modifierInfoViewModel.authenticate("testcedar", "james")
        modifierInfoViewModel.authenticate("ryan7994", "Pass1234")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ loginResponse: LoginResponse? ->
                Toast.makeText(this, loginResponse?.accessToken, Toast.LENGTH_SHORT).show()
            }, {

            }).addToCompositeDisposable()

    }

    fun onClickGetModifierGroups(view: View) {

    }

    fun onClickMenuItem(view: View) {
        val intent = Intent(this, MenuItemDetailActivity::class.java)
        startActivity(intent)
    }

    fun onClickRetrieveFromApiButton(view: View) {
//        modifierInfoViewModel.getModifierInfosFromRemote()
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe({ modifierInfoResponse ->
//                remoteModifierInfos = modifierInfoResponse.modifierList
////                updateRemoteModifierInfosText()
//            }, { error ->
//                Log.d("ERROR", error.message, error)
//                handleError(error)
//            }).addToCompositeDisposable()
    }

    fun onClickLaunchMenu(view: View) {
        val intent = MenuActivity.createIntent(this)
        startActivity(intent)
    }

    private fun handleError(error: Throwable) {
        if (error is SocketTimeoutException) {
            AlertDialog.Builder(this)
                .setMessage("The network call timed out. Please try again.")
                .setPositiveButton("OK", object : DialogInterface.OnClickListener {
                    override fun onClick(dialogInterface: DialogInterface, p1: Int) {
                        dialogInterface.dismiss()
                    }
                })
                .show()
        }
    }

    private fun Disposable.addToCompositeDisposable() {
        compositeDisposable.add(this)
    }

//    fun updateRemoteModifierInfosText() {
//        var modifierList = ""
//        remoteModifierInfos.forEach { modifierInfo -> modifierList += "${modifierInfo.modifierId} - ${modifierInfo.modifierName} - ${modifierInfo.priceDelta} - ${modifierInfo.receiptText} \n" }
//        tvModifierList.text = modifierList
//    }

//    fun updateModifierListText() {
//        var modifierList = ""
//        modifierInfosEntity.forEach { modifierInfo -> modifierList += "${modifierInfo.modifierId} - ${modifierInfo.name} - ${modifierInfo.priceDelta} - ${modifierInfo.receiptText} \n" }
//        tvModifierList.text = modifierList
//    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }
}
