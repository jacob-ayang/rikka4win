package androidx.room

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
annotation class Entity(
    val tableName: String = "",
    val indices: Array<Index> = [],
    val foreignKeys: Array<ForeignKey> = []
)

@Target(AnnotationTarget.FIELD)
annotation class PrimaryKey(val autoGenerate: Boolean = false)

@Target(AnnotationTarget.FIELD)
annotation class ColumnInfo(val name: String = "", val defaultValue: String = "")

@Target(AnnotationTarget.CLASS)
annotation class Dao

@Target(AnnotationTarget.FUNCTION)
annotation class Query(val value: String)

@Target(AnnotationTarget.FUNCTION)
annotation class Insert(val onConflict: Int = OnConflictStrategy.ABORT)

@Target(AnnotationTarget.FUNCTION)
annotation class Update

@Target(AnnotationTarget.FUNCTION)
annotation class Delete

object OnConflictStrategy {
    const val ABORT = 0
}

@Target(AnnotationTarget.CLASS)
annotation class Database(
    val entities: Array<KClass<*>>,
    val version: Int,
    val autoMigrations: Array<AutoMigration> = []
)

@Target(AnnotationTarget.CLASS)
annotation class AutoMigration(val from: Int, val to: Int, val spec: KClass<*> = Any::class)

@Target(AnnotationTarget.CLASS)
annotation class TypeConverters(vararg val value: KClass<*>)

@Target(AnnotationTarget.FUNCTION)
annotation class TypeConverter

@Target(AnnotationTarget.CLASS)
annotation class Index(vararg val value: String, val unique: Boolean = false)

@Target(AnnotationTarget.CLASS)
annotation class ForeignKey(
    val entity: KClass<*>,
    val parentColumns: Array<String>,
    val childColumns: Array<String>,
    val onDelete: Int = 0
) {
    companion object {
        const val CASCADE = 5
    }
}

@Target(AnnotationTarget.CLASS)
annotation class DeleteColumn(val tableName: String, val columnName: String)

open class RoomDatabase
