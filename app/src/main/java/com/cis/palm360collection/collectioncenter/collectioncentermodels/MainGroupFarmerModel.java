package com.cis.palm360collection.collectioncenter.collectioncentermodels;

import com.cis.palm360collection.dbmodels.PlotDetailsObj;
import com.cis.palm360collection.uihelper.expandablecheckbox.models.MultiCheckExpandableGroup;

import java.util.List;

/**
 * Created by siva on 06/04/17.
 */


//Not Using
public class MainGroupFarmerModel extends MultiCheckExpandableGroup {
    private String farmerPhoto;
    public String title;
    public MainGroupFarmerModel(String title, List<PlotDetailsObj> items, String farmerPhoto) {
        super(title, items);
        this.title = title;
        this.farmerPhoto = farmerPhoto;
    }

    public String getFarmerPhoto() {
        return farmerPhoto;
    }
}
