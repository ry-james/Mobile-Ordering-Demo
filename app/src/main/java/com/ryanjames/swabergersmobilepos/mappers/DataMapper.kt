package com.ryanjames.swabergersmobilepos.mappers

import io.realm.RealmList

interface DataMapper<Entity, Api, Domain> {

    fun mapRemoteToLocal(input: Api): Entity

    fun mapLocalToDomain(input: Entity): Domain

    fun mapDomainToLocal(input: Domain): Entity

    fun mapRemoteToLocal(input: List<Api>): List<Entity> {
        return input.map { this.mapRemoteToLocal(it) }
    }

    fun mapLocalToDomain(input: List<Entity>): List<Domain> {
        return input.map { this.mapLocalToDomain(it) }
    }

    fun mapDomainToLocal(input: List<Domain>): RealmList<Entity> {
        return RealmList<Entity>().apply { addAll(input.map { this@DataMapper.mapDomainToLocal(it) }) }
    }
}


interface DataListMapper<Entity, Api, Domain> :
    DataMapper<List<Entity>, List<Api>, List<Domain>>

interface Mapper<I, O> {
    fun map(input: I): O
}

// Non-nullable to Non-nullable
interface ListMapper<I, O> : Mapper<List<I>, List<O>>

class ListMapperImpl<I, O>(
    private val mapper: Mapper<I, O>
) : ListMapper<I, O> {
    override fun map(input: List<I>): List<O> {
        return input.map { mapper.map(it) }
    }
}

// Nullable to Non-nullable
interface NullableInputListMapper<I, O> : Mapper<List<I>?, List<O>>

class NullableInputListMapperImpl<I, O>(
    private val mapper: Mapper<I, O>
) : NullableInputListMapper<I, O> {
    override fun map(input: List<I>?): List<O> {
        return input?.map { mapper.map(it) }.orEmpty()
    }
}

// Non-nullable to Nullable
interface NullableOutputListMapper<I, O> : Mapper<List<I>, List<O>?>

class NullableOutputListMapperImpl<I, O>(
    private val mapper: Mapper<I, O>
) : NullableOutputListMapper<I, O> {
    override fun map(input: List<I>): List<O>? {
        return if (input.isEmpty()) null else input.map { mapper.map(it) }
    }
}