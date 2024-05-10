package community.flock.wirespec.compare

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.left
import arrow.core.mapOrAccumulate
import arrow.core.nel
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.zipOrAccumulate
import arrow.core.right
import community.flock.wirespec.compiler.core.parse.Definition
import community.flock.wirespec.compiler.core.parse.Endpoint
import community.flock.wirespec.compiler.core.parse.Type


object Compare {

    fun compare(left: List<Definition>, right: List<Definition>): Either<NonEmptyList<Validation>, List<Any>> {
        val list = (left to right).pairBy { it.name }
        return list.mapOrAccumulate { it.compareDefinition().bindNel() }
    }

    private fun Paired<Definition>.compareDefinition(): Either<NonEmptyList<Validation>, Any> =
        when (this) {
            is Paired.Left -> RemovedDefinitionValidation(key, left).nel().left()
            is Paired.Right -> AddedDefinitionValidation(key, right).nel().left()
            is Paired.Both -> when {
                left == right -> true.right()
                left is Type && right is Type -> (this as Paired.Both<Type>).compareType()
                left is Endpoint && right is Endpoint -> (this as Paired.Both<Endpoint>).compareEndpoint()
                else -> TODO()
            }
        }

    private fun Paired.Both<Type>.compareType(): Either<NonEmptyList<FieldValidation>, List<Boolean>> {
        val paired = (left.shape.value to right.shape.value).pairBy { it.identifier.value }
        return paired.mapOrAccumulate { it.compareField(key).bindNel() }
    }

    private fun Paired<Type.Shape.Field>.compareField(definitionKey:String): Either<NonEmptyList<FieldValidation>, Boolean> = when (this) {
        is Paired.Left -> RemovedFieldValidation("${definitionKey}.${key}", left).nel().left()
        is Paired.Right -> AddedFieldValidation("${definitionKey}.${key}", right).nel().left()
        is Paired.Both -> either {
            zipOrAccumulate(
                { ensure(left.isNullable == right.isNullable) { ChangedNullableFieldValidation("${definitionKey}.${key}", left, right) } },
                {
                    ensure(left.reference.isIterable == right.reference.isIterable) {
                        ChangedIterableFieldValidation("${definitionKey}.${key}", left, right)
                    }
                },
                {
                    ensure(left.reference.isMap == right.reference.isMap) {
                        ChangedMapFieldValidation("${definitionKey}.${key}", left, right)
                    }
                },
                {
                    ensure(left.reference.value == right.reference.value) {
                        ChangedReferenceFieldValidation("${definitionKey}.${key}", left, right)
                    }
                }
            ) { _, _, _, _ -> true }
        }
    }

//    private fun Paired.Both<Endpoint.Content>.compareContent(definitionKey:String): Either<NonEmptyList<FieldValidation>, List<Boolean>> =
//        when (this) {
//            is Paired.Left -> RemovedFieldValidation("${definitionKey}.${key}", left).nel().left()
//            is Paired.Right -> AddedFieldValidation("${definitionKey}.${key}", right).nel().left()
//            else -> {}
//        }

    private fun Paired.Both<Endpoint>.compareEndpoint(): Either<NonEmptyList<Validation>, Any> = either {
        zipOrAccumulate(
            { ensure(left.method == right.method) { MethodEndpointValidation(key, left, right) } },
            { ensure(left.path == right.path) { PathEndpointValidation(key, left, right) } },
            {
                (left.query to right.query).pairBy { it.identifier.value }
                    .mapOrAccumulate { it.compareField(key).bindNel() }
            },
            {
                (left.headers to right.headers).pairBy { it.identifier.value }
                    .mapOrAccumulate { it.compareField(key).bindNel() }
            },
            {
                (left.cookies to right.cookies).pairBy { it.identifier.value }
                    .mapOrAccumulate { it.compareField(key).bindNel() }
            },
//            {
//                (left.requests to right.requests).pairBy { it.content?.reference.value }
//                    .mapOrAccumulate { it.compareField(key).bindNel() }
//            },
        )
        { _, _, _, _, _ -> true }
    }

    sealed class Paired<A> {
        class Left<A>(val key: String, val left: A) : Paired<A>()
        class Right<A>(val key: String, val right: A) : Paired<A>()
        class Both<A>(val key: String, val left: A, val right: A) : Paired<A>()
    }

    inline fun <A> Pair<List<A>, List<A>>.pairBy(f: (a: A) -> String): List<Paired<A>> {
        val leftMap = first.groupBy { f(it) }
        val rightMap = second.groupBy { f(it) }
        val allKeys = leftMap.keys + rightMap.keys
        return allKeys.map {
            when {
                leftMap[it] == null && rightMap[it] != null -> Paired.Right(it, rightMap[it]!!.first())
                leftMap[it] != null && rightMap[it] == null -> Paired.Left(it, leftMap[it]!!.first())
                else -> Paired.Both(it, leftMap[it]!!.first(), rightMap[it]!!.first())
            }
        }.toList()
    }
}

