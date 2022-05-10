package com.serverless.forschungsprojectfaas.view.custom

import android.graphics.PointF
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object PointFSerializer: KSerializer<PointF> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("PointFSerializer", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): PointF {
        decoder.decodeString().let {

        }
        return PointF(decoder.decodeFloat(), decoder.decodeFloat())
    }

    override fun serialize(encoder: Encoder, value: PointF) {
        value.toString()
        encoder.encodeFloat(value.x)
        encoder.encodeFloat(value.y)
    }
}