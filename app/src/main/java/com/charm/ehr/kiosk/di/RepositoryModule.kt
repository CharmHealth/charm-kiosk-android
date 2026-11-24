package com.charm.ehr.kiosk.di

import com.charm.ehr.kiosk.domain.repository.FacilityRepository
import com.charm.ehr.kiosk.data.repository.FacilityRepositoryImpl
import com.charm.ehr.kiosk.domain.repository.FileRepository
import com.charm.ehr.kiosk.data.repository.FileRepositoryImpl
import com.charm.ehr.kiosk.domain.repository.MiscRepository
import com.charm.ehr.kiosk.data.repository.MiscRepositoryImpl
import com.charm.ehr.kiosk.domain.repository.PatientDetailRepository
import com.charm.ehr.kiosk.data.repository.PatientDetailRepositoryImpl
import com.charm.ehr.kiosk.domain.repository.MemberRepository
import com.charm.ehr.kiosk.data.repository.MemberRepositoryImpl
import com.charm.ehr.kiosk.domain.repository.PreferenceRepository
import com.charm.ehr.kiosk.data.repository.PreferenceRepositoryImpl
import com.charm.ehr.kiosk.domain.repository.QuestionnaireRepository
import com.charm.ehr.kiosk.data.repository.QuestionnaireRepositoryImpl
import com.charm.ehr.kiosk.domain.repository.PatientRepository
import com.charm.ehr.kiosk.data.repository.PatientRepositoryImpl
import com.charm.ehr.kiosk.domain.repository.AppointmentRepository
import com.charm.ehr.kiosk.data.repository.AppointmentRepositoryImpl
import com.charm.ehr.kiosk.domain.repository.WorkerRepository
import com.charm.ehr.kiosk.data.repository.WorkerRepositoryImpl
import com.charm.ehr.kiosk.domain.repository.OTPRepository
import com.charm.ehr.kiosk.data.repository.OTPRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindFacilityRepository(impl: FacilityRepositoryImpl): FacilityRepository

    @Binds
    @Singleton
    abstract fun bindFileRepository(impl: FileRepositoryImpl): FileRepository

    @Binds
    @Singleton
    abstract fun bindMiscRepository(impl: MiscRepositoryImpl): MiscRepository

    @Binds
    @Singleton
    abstract fun bindPatientDetailRepository(impl: PatientDetailRepositoryImpl): PatientDetailRepository

    @Binds
    @Singleton
    abstract fun bindMemberRepository(impl: MemberRepositoryImpl): MemberRepository

    @Binds
    @Singleton
    abstract fun bindPreferenceRepository(impl: PreferenceRepositoryImpl): PreferenceRepository

    @Binds
    @Singleton
    abstract fun bindQuestionnaireRepository(impl: QuestionnaireRepositoryImpl): QuestionnaireRepository

    @Binds
    @Singleton
    abstract fun bindPatientRepository(impl: PatientRepositoryImpl): PatientRepository

    @Binds
    @Singleton
    abstract fun bindAppointmentRepository(impl: AppointmentRepositoryImpl): AppointmentRepository

    @Binds
    @Singleton
    abstract fun bindWorkerRepository(impl: WorkerRepositoryImpl): WorkerRepository

    @Binds
    @Singleton
    abstract fun bindOTPRepository(impl: OTPRepositoryImpl): OTPRepository

}
