package io.paddle.plugin.python.dependencies.repositories

import io.paddle.plugin.python.utils.WrappedSerialDescriptor
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object PyPackageRepoMetadataSerializer : KSerializer<PyPackageRepository> {
    private val delegateSerializer = PyPackageRepository.Metadata.serializer()
    override val descriptor: SerialDescriptor =
        WrappedSerialDescriptor("PyPackageRepoMetadata", delegateSerializer.descriptor)

    override fun deserialize(decoder: Decoder): PyPackageRepository {
        val metadata = decoder.decodeSerializableValue(delegateSerializer)
        return PyPackageRepository(metadata)
    }

    override fun serialize(encoder: Encoder, value: PyPackageRepository) {
        encoder.encodeSerializableValue(delegateSerializer, value.metadata)
    }
}
