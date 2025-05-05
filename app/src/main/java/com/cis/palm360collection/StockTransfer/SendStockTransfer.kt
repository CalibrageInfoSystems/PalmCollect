
package com.cis.palm360collection.StockTransfer

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.cis.palm360collection.BuildConfig

import com.cis.palm360collection.cloudhelper.Log
import com.cis.palm360collection.collectioncenter.CCDataAccessHandler
import com.cis.palm360collection.collectioncenter.SendConsignment
import com.cis.palm360collection.collectioncenter.collectioncentermodels.CollectionCenter
import com.cis.palm360collection.collectioncenter.collectioncentermodels.ConsignmentRepository
import com.cis.palm360collection.common.CommonConstants
import com.cis.palm360collection.common.CommonUtils
import com.cis.palm360collection.common.FiscalDate
import com.cis.palm360collection.database.DataAccessHandler
import com.cis.palm360collection.database.DatabaseKeys.TABLE_STOCK_TRANSFER
import com.cis.palm360collection.database.Queries
import com.cis.palm360collection.datasync.helpers.DataManager
import com.cis.palm360collection.datasync.helpers.DataManager.COLLECTION_CENTER_DATA
import com.cis.palm360collection.ui.BaseFragment
import com.cis.palm360collection.utils.ImageUtility
import com.cis.palm360collection.utils.UiUtils
import com.cis.palm360collection.R
import org.apache.commons.lang3.StringUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class SendStockTransfer : BaseFragment() {
    private var grossWeightRes = 0.0
    private var tareWeightRes = 0.0
    private var netWeightRes = 0.0
    private var netWeightStr : String ? = null
    private var parentView: View? = null
    private var stockTransferNumberTv: TextView? = null
    private var fromCC: EditText? = null
    private var grossWeight: EditText? = null
    private var tareWeight: EditText? = null
    private var netWeight: EditText? = null
    private var driverNumber: EditText? = null
    private var driverName: EditText? = null
    private var comments: EditText? = null
    private var vehicleNumber: EditText? = null
    private var toCC: Spinner? = null
    private var fruitTypeSpin: Spinner? = null
    private var slipImage: ImageView? = null
    private var slipIcon: ImageView? = null
    private var finalFile: File? = null
    private var generateReceipt: Button? = null
    private var collectionCenterList: List<CollectionCenter>? = null
    private var collectionCenterId: List<CollectionCenter>? = null
    private var ccDataAccessHandler: CCDataAccessHandler? = null
    private var dataAccessHandler: DataAccessHandler? = null
    private var selectedCollectionCenter: CollectionCenter? = null
    private var mStockTransferImage: String? = null
    private var currentBitmap: Bitmap? = null
    private var stockTransferCode = ""
    private var isGrossWeight: Boolean? = false
    private var fruitCategoryTypeMap: LinkedHashMap<String, String>? = null
    private var fruitTypeCode: String? = null
    private val fruitTypeName: String? = null
    var financialYear: Int = 0
    private val PERMISSIONS_STORAGE = arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private var days = ""

    companion object {

        private val LOG_TAG = SendConsignment::class.java.name
        const val REQUEST_CAM_PERMISSIONS = 1
        private const val CAMERA_REQUEST = 1888
    }

    //Intialize the UI
    @SuppressLint("SetTextI18n")
    override fun Initialize() {
        val inflater = LayoutInflater.from(activity)
        parentView = inflater.inflate(R.layout.fragment_sendstocktransfer, null)
        baseLayout.addView(parentView, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        setTile(requireActivity().resources.getString(R.string.send_stock_transfer))
        val collectionCenterName = parentView!!.findViewById<TextView>(R.id.collection_center_name)
        val collectionCenterCode = parentView!!.findViewById<TextView>(R.id.collection_center_code)
        val collectionCenterVillage = parentView!!.findViewById<TextView>(R.id.collection_center_village)
        fromCC = parentView!!.findViewById(R.id.from_cc)
        toCC = parentView!!.findViewById(R.id.to_cc)
        fruitTypeSpin = parentView!!.findViewById(R.id.fruitTypeSpin)
        grossWeight = parentView!!.findViewById(R.id.gross_weight)
        driverName = parentView!!.findViewById(R.id.driverName)
        tareWeight = parentView!!.findViewById(R.id.tare_weight)
        netWeight = parentView!!.findViewById(R.id.net_weight)
        driverNumber = parentView!!.findViewById(R.id.driver_number)
        comments = parentView!!.findViewById(R.id.commentsEdt)
        slipImage = parentView!!.findViewById(R.id.slip_image)
        slipIcon = parentView!!.findViewById(R.id.slip_icon)
        generateReceipt = parentView!!.findViewById(R.id.generateReceipt)
        vehicleNumber = parentView!!.findViewById(R.id.vehicle_number)
        stockTransferNumberTv = parentView!!.findViewById(R.id.stockTransferNumber)


        //Getting Collection Center Details
        selectedCollectionCenter = DataManager.getInstance().getDataFromManager(COLLECTION_CENTER_DATA) as CollectionCenter
        collectionCenterName.text = selectedCollectionCenter!!.name
        collectionCenterCode.text = selectedCollectionCenter!!.code
        collectionCenterVillage.text = selectedCollectionCenter!!.villageName

        //Log.d("CollectionCenterId", selectedCollectionCenter!!.ccId + "")
        //Log.d("CollectionCenterName", selectedCollectionCenter!!.name + "")

        fromCC!!.setText(selectedCollectionCenter!!.name)
        ccDataAccessHandler = CCDataAccessHandler(activity)
        dataAccessHandler = DataAccessHandler(activity)

        val calendar = Calendar.getInstance()
        val fiscalDate = FiscalDate(calendar)
        financialYear = fiscalDate.fiscalYear

        @SuppressLint("SimpleDateFormat")
        val dateFormat = SimpleDateFormat("dd/MM/yyyy")
        try {
            val currentdate = CommonUtils.getcurrentDateTime(CommonConstants.DATE_FORMAT_3)
            val financalDate = "01/04/" + financialYear.toString()
            val date1 = dateFormat.parse(currentdate)
            val date2 = dateFormat.parse(financalDate)
            val diff = date1.time - date2.time
            val noOfDays = (TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) + 1).toString()
            days = StringUtils.leftPad(noOfDays, 3, "0")
            Log.v(LOG_TAG, "days -->$days")

        } catch (e: Exception) {
            e.printStackTrace()
        }


        //Generate StockTransfer Code
        stockTransferCode = ccDataAccessHandler!!.getGeneratedCollectionCode(selectedCollectionCenter!!.code, ccDataAccessHandler!!.STOCKTRANSFER_CODE_INITIAL, TABLE_STOCK_TRANSFER,days)

        stockTransferNumberTv!!.text = "" + stockTransferCode
        collectionCenterList = ccDataAccessHandler!!.getSTToCC(Queries.getInstance().getStToCC(CommonConstants.USER_ID))
        collectionCenterId = ccDataAccessHandler!!.getGeneratedCollectionCenterId(Queries.getInstance().getStockTransferCOLCNId(CommonConstants.USER_ID))
        val collectionCenterNames = arrayOfNulls<String>(collectionCenterList!!.size)
        for (i in collectionCenterList!!.indices) {
            collectionCenterNames[i] = collectionCenterList!![i].name
        }

        val filteredData = arrayOfNulls<String>(1)
        filteredData[0] = "-- Select To CC --"
        val list = ArrayList(Arrays.asList<String>(*filteredData))
        list.addAll(Arrays.asList<String>(*collectionCenterNames))
        val c = list.toTypedArray()
        val finalCollectionCenterNames = Arrays.copyOf<String, Any>(c, c.size, Array<String>::class.java)


        //Binding To CC Spinner Data
        val collectionSpinnerArrayAdapter = ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_item, finalCollectionCenterNames)
        collectionSpinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        toCC!!.adapter = collectionSpinnerArrayAdapter

        slipImage!!.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !CommonUtils.isPermissionAllowed(activity, Manifest.permission.CAMERA)) {
                com.cis.palm360collection.cloudhelper.Log.v(LOG_TAG, "Location Permissions Not Granted")
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    PERMISSIONS_STORAGE,
                    REQUEST_CAM_PERMISSIONS
                )
            } else {
                dispatchTakePictureIntent(CAMERA_REQUEST)
            }
        }

        //Generate Receipt On Click Listener
        generateReceipt!!.setOnClickListener {
            if (validation()) {
                stockTransferImage()
            }
        }


        //Binding Data to FruitTypeSpin& On Item Selected Listener
        fruitCategoryTypeMap = dataAccessHandler!!.getfruitData(Queries.getInstance().getfruittype())
        val spinnerArrayAdapterFruitType = ArrayAdapter(
            requireActivity(),
            android.R.layout.simple_spinner_item,
            CommonUtils.fromMap(fruitCategoryTypeMap, "Fruit Type")
        )
        spinnerArrayAdapterFruitType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        fruitTypeSpin!!.adapter = spinnerArrayAdapterFruitType

        fruitTypeSpin!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long) {
                if (fruitCategoryTypeMap != null && fruitCategoryTypeMap!!.isNotEmpty() && fruitTypeSpin!!.selectedItemPosition != 0) {
                    fruitTypeCode = fruitCategoryTypeMap!!.keys.toTypedArray<String>().get(i - 1)
                    val fruitTypeName = fruitTypeSpin!!.selectedItem.toString()
                    Log.v(LOG_TAG, "sendStockTransfer: $fruitTypeCode Fruit name: $fruitTypeName")

                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {
                // Do nothing
            }
        }

//        FruitCategoryTypeMap =
//            dataAccessHandler!!.getfruitData(Queries.getInstance().getfruittype())
//        val spinnerArrayAdapterFruitType: ArrayAdapter<*> = ArrayAdapter(
//            activity, android.R.layout.simple_spinner_item,
//            CommonUtils.fromMap(FruitCategoryTypeMap, "Fruit Type")
//        )
//        spinnerArrayAdapterFruitType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        fruitTypeSpin.setAdapter(spinnerArrayAdapterFruitType)
//
//        fruitTypeSpin.setOnItemSelectedListener(object : OnItemSelectedListener {
//            override fun onItemSelected(adapterView: AdapterView<*>?, view: View, i: Int, l: Long) {
//                if (FruitCategoryTypeMap != null && FruitCategoryTypeMap!!.size > 0 && fruitTypeSpin.getSelectedItemPosition() != 0) {
//                    fruitTypeCode = FruitCategoryTypeMap!!.keys.toTypedArray<String>()[i - 1]
//                    fruitTypeName = fruitTypeSpin.getSelectedItem().toString()
//                    android.util.Log.v(
//                        WeighbridgeCC.LOG_TAG,
//                        "@@@ Fruit  code $fruitTypeCode Fruit name $fruitTypeName"
//                    )
//                }
//            }
//            override fun onNothingSelected(adapterView: AdapterView<*>?) {
//            }
//        })

//Gross Weight Text Change Listener
        grossWeight!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {


                if (!TextUtils.isEmpty(s.toString()) || !s.toString().equals("", ignoreCase = true)) {
                    isGrossWeight = true
                    netWeight!!.isEnabled = false
                    if (!TextUtils.isEmpty(tareWeight!!.text.toString())) {
                        grossWeightRes = java.lang.Double.parseDouble(grossWeight!!.text.toString())
                        tareWeightRes = java.lang.Double.parseDouble(tareWeight!!.text.toString())
                        netWeightRes = grossWeightRes - tareWeightRes
                        netWeightStr = netWeightRes.toString()
                        netWeight!!.setText(netWeightStr!!)
                    }

                } else if (!TextUtils.isEmpty(tareWeight!!.text.toString()) || !tareWeight!!.text.toString().equals("", ignoreCase = true)) {
                    netWeight!!.setText("")
                    isGrossWeight = false
                    netWeight!!.isEnabled = false
                } else {
                    netWeight!!.setText("")
                    isGrossWeight = false
                    netWeight!!.isEnabled = true
                }
            }


        })

//Tare Weight Text Change Listener
        tareWeight!!.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {
                val count = s.toString()
                if (!TextUtils.isEmpty(grossWeight!!.text.toString()) && !TextUtils.isEmpty(tareWeight!!.text.toString())) {
                    grossWeightRes = java.lang.Double.parseDouble(grossWeight!!.text.toString())
                    tareWeightRes = java.lang.Double.parseDouble(tareWeight!!.text.toString())
                    if (grossWeightRes > tareWeightRes) {
                        netWeightRes = grossWeightRes - tareWeightRes
                        netWeightStr = netWeightRes.toString()
                        netWeight!!.setText(netWeightStr!!)
                    } else {
                        UiUtils.showCustomToastMessage("Tare Weight should be less than Gross Weight", activity, 1)
                        tareWeight!!.setText(count.substring(0, count.length - 1))

                    }


                } else {
                    Toast.makeText(activity, "Please Enter GrossWeight Value",
                        Toast.LENGTH_SHORT).show()
                    netWeight!!.setText("")
                }


            }

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {

            }
        })
    }

    //Binding Data to the Keys of Stock Transfer Table
    private fun bindStockTransferData() {
        try {

            DataManager.getInstance().addData(DataManager.To_COLLECTION_CENTER_DATA, collectionCenterList!![toCC!!.selectedItemPosition - 1])
            val stockTransferReceipt = StockTransferReceipt()
            val stockTransfer = StockTransfer()
            stockTransfer.code = stockTransferCode
            stockTransfer.fromCC = selectedCollectionCenter!!.code
            stockTransfer.grossWeight = grossWeightRes
            stockTransfer.tareWeight = tareWeightRes
            stockTransfer.netWeight = java.lang.Double.parseDouble(netWeight!!.text.toString())
            stockTransfer.driverName = driverName!!.text.toString()
            stockTransfer.vehicleNumber = vehicleNumber!!.text.toString()
            stockTransfer.weightDifference = 0.0
            stockTransfer.receiptGeneratedDate = CommonUtils.getcurrentDateTime(CommonConstants.DATE_FORMAT_DDMMYYYY_HHMMSS)
            stockTransfer.active = 1
            stockTransfer.fruitTypeId = fruitTypeCode!!.toInt()
            stockTransfer.stockUpdate = 0
            stockTransfer.totalBunches = 0
            stockTransfer.rejectedBunches = 0
            stockTransfer.acceptedBunches = 0
            stockTransfer.createdDate = CommonUtils.getcurrentDateTime(CommonConstants.DATE_FORMAT_DDMMYYYY_HHMMSS)
            stockTransfer.createdByUserId = if (!TextUtils.isEmpty(CommonConstants.USER_ID)) Integer.parseInt(CommonConstants.USER_ID) else 0
            stockTransfer.updatedByUserId = if (!TextUtils.isEmpty(CommonConstants.USER_ID)) Integer.parseInt(CommonConstants.USER_ID) else 0
            stockTransfer.updatedDate = CommonUtils.getcurrentDateTime(CommonConstants.DATE_FORMAT_DDMMYYYY_HHMMSS)
            stockTransfer.remarks = comments!!.text.toString()
            stockTransfer.serverUpdatedStatus = false
            stockTransfer.driverMobileNumber = driverNumber!!.text.toString()

            val bundle = Bundle()
            bundle.putParcelable("stockTransfer", stockTransfer)
            bundle.putString("days", days);
            stockTransferReceipt.arguments = bundle

            val backStateName = stockTransferReceipt.javaClass.name
            val fm = fragmentManager
            val ftransaction = fm!!.beginTransaction()
            ftransaction.replace(android.R.id.content, stockTransferReceipt).commit()
            ftransaction.addToBackStack(backStateName)


        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    //Binding Stock Trnasfer Image Data
    private fun stockTransferImage() {
        val stockTransferRepository = ConsignmentRepository()
        stockTransferRepository.consignmentCode = stockTransferCode
        stockTransferRepository.moduleTypeId = 331
        stockTransferRepository.fileName = stockTransferCode
        stockTransferRepository.fileLocation = mStockTransferImage
        stockTransferRepository.fileExtension = ".jpg"
        stockTransferRepository.isActive = 1
        stockTransferRepository.createdDate = CommonUtils.getcurrentDateTime(CommonConstants.DATE_FORMAT_DDMMYYYY_HHMMSS)
        stockTransferRepository.createdByUserId = if (!TextUtils.isEmpty(CommonConstants.USER_ID)) Integer.parseInt(CommonConstants.USER_ID) else 0
        stockTransferRepository.updatedByUserId = if (!TextUtils.isEmpty(CommonConstants.USER_ID)) Integer.parseInt(CommonConstants.USER_ID) else 0
        stockTransferRepository.updatedDate = CommonUtils.getcurrentDateTime(CommonConstants.DATE_FORMAT_DDMMYYYY_HHMMSS)
        stockTransferRepository.isServerUpdatedStatus = false

        DataManager.getInstance().addData(DataManager.stockTransferImage, stockTransferRepository)
        bindStockTransferData()
    }


    //Validations
    private fun validation(): Boolean {

        if (CommonUtils.isEmptySpinner(toCC)) {
            UiUtils.showCustomToastMessage("Please Select To CC", activity, 1)
            return false
        }

        if (CommonUtils.isEmptySpinner(fruitTypeSpin)) {
            UiUtils.showCustomToastMessage("Please Select fruit type", activity, 1)
            return false
        }

        if (isGrossWeight!!) {
            if (TextUtils.isEmpty(tareWeight!!.text.toString())) {
                UiUtils.showCustomToastMessage("Please Enter TareWeight", activity, 1)
                tareWeight!!.requestFocus()
                return false

            }
        }

        if (TextUtils.isEmpty(grossWeight!!.text.toString())) {
            UiUtils.showCustomToastMessage("Please Enter GrossWeight", activity, 1)
            grossWeight!!.requestFocus()
            return false

        }

        if (!TextUtils.isEmpty(tareWeight!!.text.toString())) {
            if (TextUtils.isEmpty(grossWeight!!.text.toString())) {
                UiUtils.showCustomToastMessage("Please Enter GrossWeight", activity, 1)
                grossWeight!!.requestFocus()
                return false

            }
        }

        if (TextUtils.isEmpty(driverNumber!!.text.toString())) {
            UiUtils.showCustomToastMessage("Please Enter Driver Mobile Number", activity, 1)
            //            driverNumber.setError("Please Enter Driver Mobile Number");
            driverNumber!!.requestFocus()
            return false
        }
        if (TextUtils.isEmpty(driverName!!.text.toString())) {
            UiUtils.showCustomToastMessage("Please Enter Driver Name", activity, 1)
            //            driverNumber.setError("Please Enter Driver Mobile Number");
            driverName!!.requestFocus()
            return false
        }
        if (TextUtils.isEmpty(vehicleNumber!!.text.toString())) {
            UiUtils.showCustomToastMessage("Please Enter Vehicle Number", activity, 1)
            vehicleNumber!!.requestFocus()
            return false

        }

        if (TextUtils.isEmpty(comments!!.text.toString())) {
            UiUtils.showCustomToastMessage("Please Enter Comments", activity, 1)
            comments!!.requestFocus()
            return false

        }
        if (currentBitmap == null) {
            UiUtils.showCustomToastMessage("Please Take Send Stock Receipt Photo", activity, 1)
            return false
        }
        if(fromCC!!.text.toString() == toCC!!.selectedItem.toString()){
            UiUtils.showCustomToastMessage("fromCC and toCC should not be same", activity, 1)
            return false

        }


        /*if((Double.parseDouble(driverNumber.getText().toString())>10))
        {
            UiUtils.showCustomToastMessage("Please Enter Driver Mobile Number",getActivity(),1);
//            driverNumber.setError("Please Enter Driver Mobile Number");
            driverNumber.requestFocus();
            return false;
        }*/
        return true
    }


    //Load Image on Resume
    override fun onResume() {
        super.onResume()
        if (!TextUtils.isEmpty(mStockTransferImage) && !TextUtils.isEmpty(stockTransferCode) && null != slipImage) {
            loadImageFromStorage(mStockTransferImage!!)
            slipImage!!.invalidate()
        }
    }

    //Handling Image on Activity Result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            CAMERA_REQUEST -> {
                if (resultCode == RESULT_OK) {
                    try {
                        //                        UiUtils.decodeFile(mCurrentPhotoPath,finalFile);
                        handleBigCameraPhoto()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }


                }
            }
        }
    }


    private fun handleBigCameraPhoto() {

        if (mStockTransferImage != null) {
            setPic()
            galleryAddPic()
            //            mCurrentPhotoPath = null;
        }

    }

    //Set Image to Bitmap
    private fun setPic() {

        val targetW = slipImage!!.width
        val targetH = slipImage!!.height

        /* Get the size of the image */
        val bmOptions = BitmapFactory.Options()
        bmOptions.inJustDecodeBounds = true
        BitmapFactory.decodeFile(mStockTransferImage, bmOptions)
        val photoW = bmOptions.outWidth
        val photoH = bmOptions.outHeight

        /* Figure out which way needs to be reduced less */
        var scaleFactor = 1
        if (targetW > 0 || targetH > 0) {
            scaleFactor = Math.min(photoW / targetW, photoH / targetH)
        }

        /* Set bitmap options to scale the image decode target */
        bmOptions.inJustDecodeBounds = false
        bmOptions.inSampleSize = scaleFactor
        bmOptions.inPurgeable = true

        /* Decode the JPEG file into a Bitmap */
        var bitmap = BitmapFactory.decodeFile(mStockTransferImage, bmOptions)

        bitmap = ImageUtility.rotatePicture(90, bitmap)

        currentBitmap = bitmap
        slipImage!!.setImageBitmap(bitmap)
        slipImage!!.visibility = View.VISIBLE
        slipIcon!!.visibility = View.GONE
        slipImage!!.invalidate()
    }

    private fun galleryAddPic() {
        val mediaScanIntent = Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE")
        val f = File(mStockTransferImage!!)
        val contentUri = Uri.fromFile(f)
        mediaScanIntent.data = contentUri
        requireActivity().sendBroadcast(mediaScanIntent)
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val root = Environment.getExternalStorageDirectory().toString()
        val rootDirectory = File("$root/3F_Pictures")
        val pictureDirectory = File("$root/3F_Pictures/stockTransfer")

        if (!rootDirectory.exists()) {
            rootDirectory.mkdirs()
        }

        if (!pictureDirectory.exists()) {
            pictureDirectory.mkdirs()
        }
        finalFile = File(pictureDirectory, stockTransferCode + CommonConstants.JPEG_FILE_SUFFIX)
        return finalFile as File
    }

    @Throws(IOException::class)
    private fun setUpPhotoFile(): File {

        val f = createImageFile()
        mStockTransferImage = f.absolutePath

        return f
    }

    private fun dispatchTakePictureIntent(actionCode: Int) {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        when (actionCode) {
            CAMERA_REQUEST -> {
                var f: File? = null
                mStockTransferImage = null
                try {
                    f = setUpPhotoFile()
                    mStockTransferImage = f.absolutePath
                    val photoURI = FileProvider.getUriForFile(requireActivity(),
                        BuildConfig.APPLICATION_ID + ".provider",
                        f)
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                } catch (e: IOException) {
                    e.printStackTrace()
                    f = null
                    mStockTransferImage = null
                }

            }

            else -> {
            }
        }
        startActivityForResult(takePictureIntent, actionCode)
    }


    //Load Image
    private fun loadImageFromStorage(path: String) {
        try {
            val f = File(path)
            var b = BitmapFactory.decodeStream(FileInputStream(f))
            b = ImageUtility.rotatePicture(90, b)
            slipImage!!.setImageBitmap(b)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }

    }



}
