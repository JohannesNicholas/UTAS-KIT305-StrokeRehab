package au.edu.utas.joeyn.strokerehab

import com.google.firebase.Timestamp
import com.google.type.Date

class Record(
    var title: String? = null,
    var start: Timestamp? = null,
    var reps: Int? = null,
    var buttonsOrNotches: Int? = null,
    var messages: MutableList<RecordMessage>? = null
)

data class RecordMessage(
    var datetime: Timestamp? = null,
    var message: String? = null,
    var rep: Int? = null,
    var correctPress: Boolean? = null
)