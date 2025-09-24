package com.example.poctransmissao

import fi.iki.elonen.NanoHTTPD
import java.io.File
import java.io.FileInputStream

class LocalWebServer(private val file: File) : NanoHTTPD(8080) {
    override fun serve(session: IHTTPSession?): Response {
        return newChunkedResponse(
            Response.Status.OK,
            "video/mp4",
            FileInputStream(file)
        )
    }
}
