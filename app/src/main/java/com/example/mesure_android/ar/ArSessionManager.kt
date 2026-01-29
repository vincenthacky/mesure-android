package com.example.mesure_android.ar

import com.example.mesure_android.data.model.Quaternion
import com.example.mesure_android.data.model.Vector3
import com.google.ar.core.Anchor
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import io.github.sceneview.math.Position
import io.github.sceneview.node.Node
import javax.inject.Inject
import javax.inject.Singleton

data class ArOrigin(
    val position: Vector3,
    val rotation: Quaternion,
    val anchor: Anchor? = null
)

@Singleton
class ArSessionManager @Inject constructor() {

    private var arSession: Session? = null
    private var origin: ArOrigin? = null
    private var isCalibrated: Boolean = false
    private val placedNodes = mutableListOf<Node>()

    fun configureSession(session: Session) {
        arSession = session
        val config = Config(session).apply {
            planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
            updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
            focusMode = Config.FocusMode.AUTO
        }
        session.configure(config)
    }

    fun setOrigin(position: Vector3, rotation: Quaternion, anchor: Anchor? = null) {
        origin = ArOrigin(position, rotation, anchor)
        isCalibrated = true
    }

    fun getOrigin(): ArOrigin? = origin

    fun isCalibrated(): Boolean = isCalibrated

    fun performHitTest(frame: Frame, x: Float, y: Float): HitResult? {
        val hitResults = frame.hitTest(x, y)
        return hitResults.firstOrNull { hit ->
            val trackable = hit.trackable
            trackable is Plane && trackable.isPoseInPolygon(hit.hitPose) &&
                    trackable.trackingState == TrackingState.TRACKING
        }
    }

    fun worldPositionToVector3(position: Position): Vector3 {
        return Vector3(position.x, position.y, position.z)
    }

    fun getRelativePosition(worldPosition: Vector3): Vector3 {
        val originPos = origin?.position ?: Vector3.ZERO
        return worldPosition - originPos
    }

    fun addNode(node: Node) {
        placedNodes.add(node)
    }

    fun getPlacedNodes(): List<Node> = placedNodes.toList()

    fun clearNodes() {
        placedNodes.clear()
    }

    fun resetSession() {
        origin = null
        isCalibrated = false
        clearNodes()
    }

    fun release() {
        clearNodes()
        arSession?.close()
        arSession = null
    }
}
