package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class CRMRepository(private val crmDao: CRMDao) {

    val allProducts: Flow<List<LubricantProduct>> = crmDao.getAllProducts()
    val allCustomers: Flow<List<CustomerContact>> = crmDao.getAllCustomers()
    val allVisits: Flow<List<VisitLog>> = crmDao.getAllVisits()
    val allSubmissions: Flow<List<FormSubmission>> = crmDao.getAllSubmissions()
    val allDayTracks: Flow<List<WorkerDayTrack>> = crmDao.getAllDayTracks()

    suspend fun insertProduct(product: LubricantProduct) = crmDao.insertProduct(product)
    suspend fun updateProduct(product: LubricantProduct) = crmDao.updateProduct(product)
    suspend fun deleteProductById(id: Int) = crmDao.deleteProductById(id)

    suspend fun insertCustomer(customer: CustomerContact) = crmDao.insertCustomer(customer)
    suspend fun deleteCustomerById(id: Int) = crmDao.deleteCustomerById(id)

    suspend fun insertVisit(visit: VisitLog) = crmDao.insertVisit(visit)
    suspend fun deleteVisitById(id: Int) = crmDao.deleteVisitById(id)

    suspend fun insertSubmission(submission: FormSubmission) = crmDao.insertSubmission(submission)
    suspend fun updateSubmission(submission: FormSubmission) = crmDao.updateSubmission(submission)
    suspend fun deleteSubmissionById(id: Int) = crmDao.deleteSubmissionById(id)

    suspend fun insertDayTrack(track: WorkerDayTrack) = crmDao.insertDayTrack(track)
    suspend fun updateDayTrack(track: WorkerDayTrack) = crmDao.updateDayTrack(track)
    suspend fun deleteDayTrackById(id: Int) = crmDao.deleteDayTrackById(id)

    // Pre-populate database with default items for visual polish on cold launch
    suspend fun prepopulateIfEmpty() {
        // Check products
        val currentProducts = crmDao.getAllProducts().first()
        if (currentProducts.isEmpty()) {
            val defaultProducts = listOf(
                LubricantProduct(name = "Paraffinic Pure Base Petroleum Oil", category = "Petroleum & Base Oils", packSize = "5L Can", availableStock = 140, soldStock = 420, unitPrice = 650.00),
                LubricantProduct(name = "Crude Additive Booster Oil", category = "Petroleum & Base Oils", packSize = "1L Bottle", availableStock = 300, soldStock = 890, unitPrice = 199.00),
                
                LubricantProduct(name = "RideForce 4T Synth Sport 10W-30", category = "2-Wheeler Oil", packSize = "1L Can", availableStock = 450, soldStock = 1800, unitPrice = 345.00),
                LubricantProduct(name = "Scooter Super-Zip 4T 10W-40", category = "2-Wheeler Oil", packSize = "800mL Bottle", availableStock = 520, soldStock = 2200, unitPrice = 295.00),
                
                LubricantProduct(name = "LubeStar 3T CNG Active Rickshaw", category = "3-Wheeler Oil", packSize = "1L Bottle", availableStock = 240, soldStock = 1100, unitPrice = 310.00),
                LubricantProduct(name = "TriForce Heavy Duty 3W Engine Oil", category = "3-Wheeler Oil", packSize = "3L Can", availableStock = 150, soldStock = 650, unitPrice = 880.00),
                
                LubricantProduct(name = "EcoDrive Full Synth 5W-30 Premium", category = "4-Wheeler Oil", packSize = "4L Gallon", availableStock = 180, soldStock = 920, unitPrice = 1650.00),
                LubricantProduct(name = "Cruiser Supreme Multi-Grade 15W-40", category = "4-Wheeler Oil", packSize = "1L Pack", availableStock = 320, soldStock = 1400, unitPrice = 390.00),
                
                LubricantProduct(name = "FrigidShield Extreme Green Coolant", category = "Coolants", packSize = "1L Bottle", availableStock = 380, soldStock = 1550, unitPrice = 175.00),
                LubricantProduct(name = "UltraCool Concentrate Pink Coolant", category = "Coolants", packSize = "3L Can", availableStock = 210, soldStock = 830, unitPrice = 450.00),
                
                LubricantProduct(name = "VoltFlow Demineralized Battery Water", category = "Battery Water / Fluids", packSize = "5L Bottle", availableStock = 600, soldStock = 3400, unitPrice = 90.00),
                LubricantProduct(name = "RedAcid Lead-Acid Battery Top-Up", category = "Battery Water / Fluids", packSize = "1L Dispenser", availableStock = 400, soldStock = 1680, unitPrice = 120.00),
                
                LubricantProduct(name = "GearForce EP-90 Hypoid Lube", category = "Gear & Transmission", packSize = "1L Can", availableStock = 280, soldStock = 1250, unitPrice = 275.00),
                LubricantProduct(name = "GlideShift Auto Transmission Fluid", category = "Gear & Transmission", packSize = "1L Bottle", availableStock = 190, soldStock = 780, unitPrice = 420.00)
            )
            for (p in defaultProducts) {
                crmDao.insertProduct(p)
            }
        }

        // Check customers
        val currentCustomers = crmDao.getAllCustomers().first()
        if (currentCustomers.isEmpty()) {
            val defaultCustomers = listOf(
                CustomerContact(name = "Golden Wheels Service Station", contactPerson = "Sanjay Mehta", phone = "+91-98765-43210", type = "Mechanic Garage", address = "Sector 4, Industrial Hub, Pune", shopLocationLink = "https://maps.google.com/?q=Sector+4,+Industrial+Hub,+Pune"),
                CustomerContact(name = "Apex Lubricants & Spares", contactPerson = "Arvinder Singh", phone = "+91-99887-76655", type = "Retailer Dealer", address = "Lucknow Road, Kanpur", shopLocationLink = "https://maps.google.com/?q=Lucknow+Road,+Kanpur"),
                CustomerContact(name = "Super-Speed Fleet Operators", contactPerson = "Rajiv Yadav", phone = "+91-91234-56789", type = "Fleet Account", address = "Depot, Gurgaon", shopLocationLink = "https://maps.google.com/?q=Fleet+Depot,+Gurgaon"),
                CustomerContact(name = "Metro Oils & Fluid Distributors", contactPerson = "Vikram Patel", phone = "+91-88776-55443", type = "Distributor Shop", address = "GIDC Estate, Phase II, Ahmedabad", shopLocationLink = "https://maps.google.com/?q=GIDC+Estate,+Phase+II,+Ahmedabad")
            )
            for (c in defaultCustomers) {
                crmDao.insertCustomer(c)
            }
        }

        // Check visit logs
        val currentVisits = crmDao.getAllVisits().first()
        if (currentVisits.isEmpty()) {
            val defaultVisits = listOf(
                VisitLog(
                    customerName = "Golden Wheels Service Station",
                    representativeName = "Rahul Sharma (Sales Rep)",
                    visitType = "Technical Support",
                    notes = "Conducted a seminar for 15 mechanics in the local garage on the benefits of our lithium complex greases. Highly positive feedback; they reported smoother bearing heat management during tests. Expected regular bulk orders.",
                    interestedProduct = "Multi-Purpose Lithium Grease",
                    orderQuantity = 50,
                    wasOrderPlaced = true,
                    simulatedLocation = "18.5204° N, 73.8567° E (Pune, MH)",
                    customerResponseStatus = "Ready to Buy",
                    closingRequirements = "Demonstrate product compatibility with high-speed CNC gears and provide 3 small trial grease tubes."
                ),
                VisitLog(
                    customerName = "Apex Lubricants & Spares",
                    representativeName = "Rahul Sharma (Sales Rep)",
                    visitType = "Order Booking",
                    notes = "Owner wants to stock up ahead of monsoon season. Booked orders for heavy engine and gear oils. Inquired if we could print special promotional banners with their store logo.",
                    interestedProduct = "Titan Heavy Diesel 15W-40",
                    orderQuantity = 40,
                    wasOrderPlaced = true,
                    simulatedLocation = "26.4499° N, 80.3319° E (Kanpur, UP)",
                    customerResponseStatus = "Negotiating",
                    closingRequirements = "Provide an 8x3 custom-printed shop front banner with LubeCRM co-branding."
                ),
                VisitLog(
                    customerName = "Metro Oils & Fluid Distributors",
                    representativeName = "Amit Verma (Territory Manager)",
                    visitType = "Routine Checkup",
                    notes = "Visited to audit stock clearance rates. Our Hydraulic ISO 46 is selling faster than expected due to nearby forging industry demands. Owner requested a custom online order sheet link for their corporate customers.",
                    interestedProduct = "Industrial Hydropower 46",
                    orderQuantity = 10,
                    wasOrderPlaced = true,
                    simulatedLocation = "23.0225° N, 72.5714° E (Ahmedabad, GJ)",
                    customerResponseStatus = "Warm Lead",
                    closingRequirements = "Needs 5% margin discount approval on bulk container drums to sign exclusive distributor contract."
                )
            )
            for (v in defaultVisits) {
                crmDao.insertVisit(v)
            }
        }

        // Check day tracks
        val currentTracks = crmDao.getAllDayTracks().first()
        if (currentTracks.isEmpty()) {
            val now = System.currentTimeMillis()
            val dayInMillis = 86400000L
            val defaultTracks = listOf(
                WorkerDayTrack(
                    dateString = "Yesterday (10 Jun 2026)",
                    startTime = now - dayInMillis - 28800000L, // 8 hours duration yesterday
                    endTime = now - dayInMillis,
                    kmTraveled = 34.2,
                    status = "Completed",
                    visitedCount = 3,
                    routePointsString = "18.5204,73.8567,09:15 AM;18.5392,73.8112,12:30 PM;18.5122,73.8904,03:45 PM;18.5204,73.8567,05:30 PM"
                ),
                WorkerDayTrack(
                    dateString = "09 Jun 2026",
                    startTime = now - (dayInMillis * 2) - 25200000L, // 7 hours duration
                    endTime = now - (dayInMillis * 2),
                    kmTraveled = 21.8,
                    status = "Completed",
                    visitedCount = 2,
                    routePointsString = "26.4499,80.3319,10:00 AM;26.4678,80.3122,01:15 PM;26.4499,80.3319,04:45 PM"
                )
            )
            for (t in defaultTracks) {
                crmDao.insertDayTrack(t)
            }
        }
    }
}
