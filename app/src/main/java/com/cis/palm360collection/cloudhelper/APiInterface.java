package com.cis.palm360collection.cloudhelper;

import com.cis.palm360collection.dbmodels.FingerprintModel;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface APiInterface {

    @FormUrlEncoded
    @POST("Grader/UpdateGraderFingerPrint")
    Call<FingerprintModel> postGraderFingerprints(@Field("GraderCode") String GraderCode, @Field("FingerPrintData1") String FingerPrintData1, @Field("FingerPrintData2") String FingerPrintData2, @Field("FingerPrintData3") String FingerPrintData3);
}
