package com.dtz.safestroute.event

import com.dtz.safestroute.net.gson.RedAreas

/**
 * Created by duenytz on 5/20/17.
 */
class OnRedAreasCalculated(val involvedRedAreas: MutableList<RedAreas.RedArea>, val avoidAreasString: String, val notify: Boolean)