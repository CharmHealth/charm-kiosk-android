package com.charm.ehr.kiosk.domain.usecase.appointment

import javax.inject.Inject

data class AppointmentUseCases @Inject constructor(
    val fetchAppointments: FetchAppointmentsUseCase,
    val updateAppointmentStatus: UpdateAppointmentStatusUseCase
)
