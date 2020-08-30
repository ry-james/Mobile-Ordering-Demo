package com.ryanjames.swabergersmobilepos.mappers

import io.realm.RealmList

interface DataMapper<LocalDb, Api, Domain> {

    fun mapRemoteToLocalDb(input: Api): LocalDb

    fun mapLocalDbToDomain(input: LocalDb): Domain

    fun mapDomainToLocalDb(input: Domain): LocalDb

    fun mapRemoteToLocalDb(input: List<Api>): List<LocalDb> {
        return input.map { this.mapRemoteToLocalDb(it) }
    }

    fun mapLocalDbToDomain(input: List<LocalDb>): List<Domain> {
        return input.map { this.mapLocalDbToDomain(it) }
    }

    fun mapDomainToLocalDb(input: List<Domain>): RealmList<LocalDb> {
        return RealmList<LocalDb>().apply { addAll(input.map { this@DataMapper.mapDomainToLocalDb(it) }) }
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