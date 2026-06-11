package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CRMDao {
    
    // ===== LUBRICANT PRODUCTS =====
    @Query("SELECT * FROM lubricant_products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<LubricantProduct>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: LubricantProduct)
    
    @Update
    suspend fun updateProduct(product: LubricantProduct)
    
    @Query("DELETE FROM lubricant_products WHERE id = :id")
    suspend fun deleteProductById(id: Int)
    
    // ===== CUSTOMER CONTACTS =====
    @Query("SELECT * FROM customer_contacts ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<CustomerContact>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerContact)
    
    @Query("DELETE FROM customer_contacts WHERE id = :id")
    suspend fun deleteCustomerById(id: Int)
    
    // ===== VISIT LOGS =====
    @Query("SELECT * FROM visit_logs ORDER BY visitDate DESC")
    fun getAllVisits(): Flow<List<VisitLog>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisit(visit: VisitLog)
    
    @Query("DELETE FROM visit_logs WHERE id = :id")
    suspend fun deleteVisitById(id: Int)

    // ===== FORM SUBMISSIONS =====
    @Query("SELECT * FROM form_submissions ORDER BY receivedDate DESC")
    fun getAllSubmissions(): Flow<List<FormSubmission>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubmission(submission: FormSubmission)
    
    @Update
    suspend fun updateSubmission(submission: FormSubmission)
    
    @Query("DELETE FROM form_submissions WHERE id = :id")
    suspend fun deleteSubmissionById(id: Int)

    // ===== WORKER DAY TRACKS =====
    @Query("SELECT * FROM worker_day_tracks ORDER BY startTime DESC")
    fun getAllDayTracks(): Flow<List<WorkerDayTrack>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDayTrack(track: WorkerDayTrack): Long

    @Update
    suspend fun updateDayTrack(track: WorkerDayTrack)

    @Query("DELETE FROM worker_day_tracks WHERE id = :id")
    suspend fun deleteDayTrackById(id: Int)
}
