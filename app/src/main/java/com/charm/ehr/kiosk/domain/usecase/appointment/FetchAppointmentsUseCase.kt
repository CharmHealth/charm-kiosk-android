package com.charm.ehr.kiosk.domain.usecase.appointment

import com.charm.ehr.kiosk.domain.Appointment
import com.charm.ehr.kiosk.domain.repository.AppointmentRepository
import com.charm.ehr.kiosk.presentation.composable.APIState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.joda.time.LocalDate
import javax.inject.Inject

/**
 * Fetches today's appointments for a given patient.
 *
 * Encapsulates the "today's date" business rule so callers do not need to
 * construct a [LocalDate] themselves, and the date logic is testable in isolation.
 */
class FetchAppointmentsUseCase @Inject constructor(
    private val appointmentRepository: AppointmentRepository
) {
    suspend operator fun invoke(patientId: String): APIState<ArrayList<Appointment>> =
        withContext(Dispatchers.IO) {
            appointmentRepository.fetchAppointments(
                patientId = patientId,
                dateForTheAppointment = LocalDate.now()
            )
        }
}
