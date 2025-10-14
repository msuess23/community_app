package com.example.community_app.util

enum class Role { CITIZEN, OFFICER, ADMIN }

enum class InfoStatus { SCHEDULED, ACTIVE, DONE, CANCELLED }
enum class InfoCategory { EVENT, CONSTRUCTION, MAINTENANCE, ANNOUNCEMENT, OTHER }

enum class TicketStatus { OPEN, IN_PROGRESS, RESOLVED, REJECTED, CANCELLED }
enum class TicketCategory { INFRASTRUCTURE, CLEANING, SAFETY, NOISE, OTHER }
enum class TicketVisibility { PUBLIC, PRIVATE }

enum class StatusScope { INFO, TICKET }