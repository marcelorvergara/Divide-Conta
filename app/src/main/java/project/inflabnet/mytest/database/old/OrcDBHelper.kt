package project.inflabnet.mytest.database.old

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper

class OrcDBHelper(context: Context) : SQLiteOpenHelper(context,
    DATABASE_NAME, null,
    DATABASE_VERSION
) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    @Throws(SQLiteConstraintException::class)
    fun insertOrcamento(orcamento: String): Boolean {
        // Gets the data repository in write mode
        val db = writableDatabase
        // Create a new map of values, where column names are the keys
        val values = ContentValues()
        values.put(DBContract.OsOrcamentos.COLUMN_VALOR, orcamento)
        // Insert the new row, returning the primary key value of the new row
        val newRowId = db.insert(DBContract.OsOrcamentos.TABLE_NAME, null, values)

        return true
    }

    fun readOrcamentos(): String {
        var orc = String()
        val db = writableDatabase
        var cursor: Cursor?
        try {
            cursor = db.rawQuery("select * from " + DBContract.OsOrcamentos.TABLE_NAME, null)
        } catch (e: SQLiteException) {
            // if table not yet present, create it
            db.execSQL(SQL_CREATE_ENTRIES)
            return orc
        }


        if (cursor!!.moveToFirst()) {
            while (!cursor.isAfterLast) {
                orc = cursor.getString(cursor.getColumnIndex(DBContract.OsOrcamentos.COLUMN_VALOR))
                cursor.moveToNext()
            }
        }
        return orc
    }
    @Throws(SQLiteConstraintException::class)
    fun deleteOrcamento(): Boolean {
        // Gets the data repository in write mode
        val db = writableDatabase
        // Define 'where' part of query.
        //val selection = DBContract.OsOrcamentos.COLUMN_VALOR + " LIKE ?"
        val selection = DBContract.OsOrcamentos.COLUMN_VALOR
        // Specify arguments in placeholder order.
        //val selectionArgs = arrayOf(orc)
        // Issue SQL statement.
        db.delete(DBContract.OsOrcamentos.TABLE_NAME, null, null)

        return true
    }
    companion object {
        // If you change the database schema, you must increment the database version.
        val DATABASE_VERSION = 1
        val DATABASE_NAME = "FeedReaderOrcamentos.db"

        private val SQL_CREATE_ENTRIES =
            "CREATE TABLE " + DBContract.OsOrcamentos.TABLE_NAME + " (" +
                    DBContract.OsOrcamentos.COLUMN_VALOR + " TEXT)"

        private val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + DBContract.OsOrcamentos.TABLE_NAME
    }

}