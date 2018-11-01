package com.example.lenovo.cameraapplication;

import android.os.Bundle;

import com.tencent.mm.opensdk.modelmsg.GetMessageFromWX;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXTextObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

/**
 * Created by lenovo on 2018/10/28.
 */

public class Weixin {
    private static final String App_ID = "";
    private String text = "";

    private IWXAPI api;

    private void regToWx(){
        api = WXAPIFactory.createWXAPI(MyApplication.getContext(), App_ID,true);
        api.registerApp(App_ID);
    }
    private void setreqtoWX(){
        WXTextObject textObj = new WXTextObject();
        textObj.text =text;

        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = textObj;
        msg.description = text;

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = String.valueOf(System.currentTimeMillis());
        req.message = msg;

        api.sendReq(req);
    }
    private void setresptoWX(){
        WXTextObject textObj = new WXTextObject();
        textObj.text = text;
        Bundle bundle = new Bundle();

        WXMediaMessage msg = new WXMediaMessage(textObj);
        msg.description = text;

        GetMessageFromWX.Resp resp = new GetMessageFromWX.Resp();

        resp.transaction = new GetMessageFromWX.Req(bundle).transaction;
        resp.message =msg;

        api.sendResp(resp);
    }

}

