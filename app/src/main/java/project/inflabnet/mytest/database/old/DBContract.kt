package project.inflabnet.mytest.database.old

import android.provider.BaseColumns

class DBContract {
    class OsOrcamentos : BaseColumns {
        companion object {
            val TABLE_NAME = "orcamento"
            val COLUMN_VALOR = "quanto"
        }
    }
}