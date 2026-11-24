package com.charm.ehr.kiosk.domain.usecase.appointment

import com.charm.ehr.kiosk.domain.repository.AppointmentRepository
import com.charm.ehr.kiosk.presentation.composable.APIState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Updates the status of a single appointment.
 *
 * The caller is responsible for fan-out across multiple appointments
 * (e.g. via [async]/[awaitAll]). Keeping this use case focused on a single
 * update makes it independently testable and reusable.
 */
class UpdateAppointmentStatusUseCase @Inject constructor(
    private val appointmentRepository: AppointmentRepository
) {
    suspend operator fun invoke(appointmentId: String, statusId: String): APIState<Boolean> =
        withContext(Dispatchers.IO) {
            appointmentRepository.updateAppointmentStatus(appointmentId, statusId)
        }
}
