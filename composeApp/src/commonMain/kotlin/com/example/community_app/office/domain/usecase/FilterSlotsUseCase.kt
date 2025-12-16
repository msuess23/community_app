package com.example.community_app.office.domain.usecase

import com.example.community_app.appointment.domain.Slot
import com.example.community_app.core.util.addDays
import com.example.community_app.core.util.parseIsoToMillis

class FilterSlotsUseCase {
  operator fun invoke(
    allSlots: List<Slot>,
    selectedDateMillis: Long
  ): List<Slot> {
    val nextDayMillis = addDays(selectedDateMillis, 1)

    return allSlots.filter { slot ->
      val slotStart = parseIsoToMillis(slot.startIso)
      slotStart in selectedDateMillis until nextDayMillis
    }.sortedBy { it.startIso }
  }
}