package com.charm.ehr.kiosk.domain.repository

import com.charm.ehr.kiosk.domain.Appointment
import com.charm.ehr.kiosk.presentation.composable.APIState
import org.joda.time.LocalDate

interface AppointmentRepository {
    suspend fun fetchAppointments(
        patientId: String,
        dateForTheAppointment: LocalDate
    ): APIState<ArrayList<Appointment>>
    suspend fun updateAppointmentStatus(
        appointmentId: String,
        statusId: String
    ): APIState<Boolean>
}
