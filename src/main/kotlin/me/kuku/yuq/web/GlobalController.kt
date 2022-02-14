package me.kuku.yuq.web

import com.IceCreamQAQ.Yu.annotation.After
import com.IceCreamQAQ.Yu.annotation.Global
import com.IceCreamQAQ.YuWeb.H
import com.IceCreamQAQ.YuWeb.annotation.WebController

@WebController
class GlobalController {

    @After
    @Global
    fun access(response: H.Response) {
//        response.header["Access-Control-Allow-Origin"] = "*"
    }

}