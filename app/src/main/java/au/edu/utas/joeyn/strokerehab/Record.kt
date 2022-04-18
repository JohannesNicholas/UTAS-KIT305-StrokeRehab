package au.edu.utas.joeyn.strokerehab

import com.google.firebase.Timestamp
import com.google.type.Date

data class Record(val title: String, val messages: MutableList<RecordMessage>)

data class RecordMessage(val datetime: Timestamp, val message: String)