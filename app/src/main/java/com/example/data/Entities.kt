package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lubricant_products")
data class LubricantProduct(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String, // e.g., "Motorcycle Oil", "Heavy Duty Engine Oil", "Gear & Transmission Oil", "Hydraulic Oil", "Grease"
    val packSize: String, // e.g., "1L", "5L", "20L Barrel", "210L Drum"
    val availableStock: Int,
    val soldStock: Int,
    val unitPrice: Double
)

@Entity(tableName = "customer_contacts")
data class CustomerContact(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String, // e.g., "Apex Auto Parts Shop"
    val contactPerson: String, // e.g., "Rajesh Sharma"
    val phone: String,
    val type: String, // "Retailer Dealer", "Distributor Shop", "Mechanic Garage", "Fleet Account"
    val address: String,
    val shopLocationLink: String = ""
)

@Entity(tableName = "visit_logs")
data class VisitLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerName: String,
    val representativeName: String,
    val visitDate: Long = System.currentTimeMillis(),
    val visitType: String, // "Routine Checkup", "Order Booking", "Payment Collection", "Technical Support"
    val notes: String,
    val interestedProduct: String, // Link to a product name or custom string
    val orderQuantity: Int = 0, // Booked stock count
    val wasOrderPlaced: Boolean = false,
    val simulatedLocation: String = "GPS Captured",
    val customerResponseStatus: String = "Neutral",
    val closingRequirements: String = ""
)

@Entity(tableName = "form_submissions")
data class FormSubmission(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val formType: String, // "Customer Order", "Mechanic Loyalty", "Contact Inquiry"
    val senderName: String,
    val contactInfo: String,
    val content: String, // JSON or formatted text of input
    val receivedDate: Long = System.currentTimeMillis(),
    val isProcessed: Boolean = false
)

@Entity(tableName = "worker_day_tracks")
data class WorkerDayTrack(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dateString: String,
    val startTime: Long,
    val endTime: Long = 0L,
    val kmTraveled: Double = 0.0,
    val status: String, // "Active" or "Completed"
    val visitedCount: Int = 0,
    val routePointsString: String = "" // "latitude,longitude,timestampString;latitude,longitude,timestampString..."
)
