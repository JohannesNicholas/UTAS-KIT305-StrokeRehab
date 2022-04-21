package au.edu.utas.joeyn.strokerehab

import com.google.firebase.Timestamp
import com.google.type.Date

data class Record(
    var title: String? = null,
    var start: Timestamp? = null,
    var reps: Int? = null,
    var buttons: Int? = null,
    var messages: MutableList<RecordMessage>? = null
)

data class RecordMessage(
    var datetime: Timestamp? = null,
    var message: String? = null
)