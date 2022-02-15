package io.paddle.plugin.python.dependencies.index

import io.paddle.plugin.python.utils.WrappedSerialDescriptor
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object PyPackageRepoMetadataSerializer : KSerializer<PyPackagesRepository> {
    private val delegateSerializer = PyPackagesRepository.Metadata.serializer()
    override val descriptor: SerialDescriptor =
        WrappedSerialDescriptor("PyPackageRepoMetadata", delegateSerializer.descriptor)

    override fun deserialize(decoder: Decoder): PyPackagesRepository {
        val metadata = decoder.decodeSerializableValue(delegateSerializer)
        return PyPackagesRepository(metadata.url, metadata.name)
    }

    override fun serialize(encoder: Encoder, value: PyPackagesRepository) {
        encoder.encodeSerializableValue(delegateSerializer, value.metadata)
    }
}
