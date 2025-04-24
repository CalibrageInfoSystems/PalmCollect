package com.cis.palm360collection.StockTransfer

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

//Stock Transfer Receive Model
class StockTransferReceive {

    @Expose
    @SerializedName("Code")
    var code: String? = null

    @Expose
    @SerializedName("IsStockUpdate")
    var stockUpdate: Int = 1

    @Expose
    @SerializedName("UpdatedByUserId")
    var updatedByUserId: Int = 0

    @Expose
    @SerializedName("UpdatedDate")
    var updatedDate: String? = null

    @SerializedName("ServerUpdatedStatus")
    @Expose()
    var serverUpdatedStatus: Boolean = false

}