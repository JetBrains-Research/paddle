package io.paddle.schema.builder

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import io.paddle.schema.extensions.JsonSchemaPart


class JsonSchemaBuilder(baseContent: String) {
    private val objectMapper = ObjectMapper(JsonFactory())
    private val jsonSchema = objectMapper.readTree(baseContent)

    fun append(jsonSchemaPart: JsonSchemaPart): JsonSchemaBuilder {
        val valueToInsert = try {
            objectMapper.readTree(jsonSchemaPart.content)
        } catch (e: JsonProcessingException) {
            objectMapper.valueToTree(jsonSchemaPart.content)
        }
        var targetNode = jsonSchema.at(jsonSchemaPart.destination)
        if (targetNode.isArray) {
            val arrayNode = targetNode as ArrayNode
            if (!arrayNode.contains(valueToInsert)) {
                arrayNode.add(valueToInsert)
            }
        } else {
            targetNode = jsonSchema.at(jsonSchemaPart.destination.substringBeforeLast("/"))
            if (targetNode.isObject) {
                val key = jsonSchemaPart.destination.substringAfterLast("/")
                (targetNode as ObjectNode).replace(key, valueToInsert)
            }
            if (targetNode.isArray) {
                val index = jsonSchemaPart.destination.substringAfterLast("/").toInt()
                (targetNode as ArrayNode).set(index, valueToInsert)
            }
        }
        return this
    }

    override fun toString(): String = jsonSchema.toPrettyString()
}

