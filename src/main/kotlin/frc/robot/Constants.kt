/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot

import edu.wpi.first.networktables.*
import frc.robot.commands.TurnToAngle
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard

/**
 * Constants Managment:
 *
 * Constants are stored in a hashmap
 * On robotinit, constants are loaded from persistent networktable entries
 * If no saved entry is found, a hardcoded default is used
 * code using constants may register listeners for any constant changes (useful for tuning parameters at runtime, like pid loops)
 */

private data class ConstantsListener (val func: () -> Unit, val id: Int)

class Constants {
  companion object {
    val constants = mutableMapOf(
            "DrivetrainPID_P" to 0.035,
            "DrivetrainPID_I" to 0.0,
            "DrivetrainPID_D" to 0.005,
            "DrivetrainPID_AngleToleranceDeg" to 2.0,
            "DrivetrainPID_AngleRateToleranceDegPerS" to 1.0,
            "DrivetrainSpeedCoeff_Left" to 1.0,
            "DrivetrainSpeedCoeff_Right" to 1.0,
            "IntakeBaseSpeed" to 0.0
    )

    /** NetworkTables Constants Management **/
    private lateinit var table: NetworkTable
    private var listeners = mutableListOf<ConstantsListener>()
    private var listenersId = 0

    /**
     * Load constants from network tables
     */
    fun loadConstants() {
      table = NetworkTableInstance.getDefault().getTable("Preferences")
      for(key in constants.keys) {
        if(constants[key] == null) continue

        val entry = table.getEntry(key)
        entry.setDefaultDouble(constants[key] ?: 0.0)
        entry.setPersistent()
        constants[key] = entry.getDouble(constants[key] ?: 0.0)

        entry.addListener({ event ->
          onNetworkTablesChange()
        }, EntryListenerFlags.kNew or EntryListenerFlags.kUpdate)
      }
    }

    private fun onNetworkTablesChange() {
      /** update map **/
      for(key in constants.keys) {
        val entry = table.getEntry(key)
        constants[key] = entry.getDouble(constants[key] ?: 0.0)
      }
      listeners.map {listen -> listen.func()}
    }

    /**
     * Add a listener for any changes in the Preferences NetworkTable
     * listeners will be called on any change
     * Return an id for the listener that can latter be passed to removeListener
     */
    fun addListener(listener: () -> Unit): Int {
      val id = listenersId++
      listeners.add(ConstantsListener(listener, id))
      return id
    }

    /**
     * Remove a listener with an id created by a call to AddListener
     */
    fun removeListener(id: Int) {
      listeners.removeIf {listen -> listen.id == id}
    }
  }
}
