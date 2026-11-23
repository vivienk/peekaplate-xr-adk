package com.peekaplate.market

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pico.spatial.core.ecs.Entity
import com.pico.spatial.core.ecs.TransformComponent
import com.pico.spatial.core.math.Vector3
import com.pico.spatial.core.math.EulerAngles
import com.pico.spatial.ui.design.*
import com.pico.spatial.ui.foundation.content.SpatialView
import com.pico.spatial.ui.foundation.dsl.*
import com.pico.spatial.ui.platform.stub.SpatialLaunchActivity
import org.json.JSONArray

private var requestedModel by mutableStateOf<String?>(null)

class LaunchActivity : SpatialLaunchActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        requestedModel = intent.getStringExtra("model")
        super.onCreate(savedInstanceState)
    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        requestedModel = intent.getStringExtra("model")
    }
}
class MarketApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (getProcessName() == packageName) launch(::marketApp)
    }
}

fun marketApp(scope: SpatialAppScope) = with(scope) {
    DefaultWindowContainer { PicoTheme { Market() } }
}

private data class Dish(val venue: String, val name: String, val description: String, val price: String, val model: String)

private fun menu(context: Context): List<Dish> {
    val venues = mapOf("r1-ramen" to "Noka Ramen", "r3-thai" to "Farm House Thai")
    val data = JSONArray(context.assets.open("supplied-dishes.json").bufferedReader().use { it.readText() })
    return (0 until data.length()).map { index ->
        val row = data.getJSONObject(index)
        Dish(venues.getValue(row.getString("restaurantId")), row.getString("name"),
            row.getString("description"), row.getString("price"),
            row.getJSONObject("xr").getString("model").substringAfterLast('/'))
    }
}

@Composable
private fun Market() {
    val context = LocalContext.current
    val dishes = remember { menu(context) }
    var route by remember(requestedModel) { mutableStateOf(MarketRoute(requestedModel, requestedModel != null)) }
    val selected = dishes.find { it.model == route.model }
    val portion = route.portion
    var rotation by remember { mutableFloatStateOf(0f) }
    var ready by remember { mutableStateOf(false) }
    val models = remember { mutableMapOf<String, Entity>() }
    DisposableEffect(Unit) {
        onDispose {
            models.values.forEach { if (it.valid) { it.removeFromParent(); it.destroy() } }
            models.clear()
        }
    }
    SpatialView(
        initial = { content, attachments ->
            attachments.entity("menu")?.let { panel ->
                panel.components[TransformComponent::class.java]?.setPosition(Vector3(0f, .45f, .15f))
                content.addEntity(panel)
            }
            for (dish in dishes) {
                try {
                    val model = Entity.loadSuspend(uriString = "asset://" + dish.model)
                    model.enabled = false
                    // Normalize each bundled mesh to the XR client's 27 cm reference plate.
                    val ramen = dish.model == "noka-steak-ramen.glb"
                    val scale = .27f / if (ramen) 1.6762f else .9811f
                    val baseOffset = if (ramen) .9544f * scale else 0f
                    model.components[TransformComponent::class.java]?.apply {
                        setScaleVector(Vector3(scale))
                        setPosition(Vector3(0f, -.12f + baseOffset, .18f))
                    }
                    content.addEntity(model)
                    models[dish.model] = model
                } catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
                  catch (error: Exception) { android.util.Log.e("PeekaPlate", dish.model, error) }
            }
            ready = true
        },
        update = { _, _ ->
            models.forEach { (name, model) ->
                model.enabled = portion && name == selected?.model
                if (model.enabled) model.components[TransformComponent::class.java]?.setEulerAngles(EulerAngles(0f, rotation, 0f))
            }
        },
        attachments = {
            AttachmentPanel(id = "menu") {
                Column(Modifier.width(620.dp).background(Color(0xff241c29)).padding(24.dp)) {
                    val dish = selected
                    Text(if (dish == null) "Night Market 2049" else dish.venue, color = Color.White, fontSize = 32.sp)
                    if (dish == null) {
                        dishes.forEach { item ->
                            Button(onClick = { route = route.open(item.model) }) { Text(item.venue + " · " + item.name) }
                        }
                    } else {
                        Text(dish.name + " · " + dish.price, color = Color.White, fontSize = 25.sp)
                        Text(dish.description, color = Color.White, fontSize = 18.sp)
                        if (portion) {
                            Text("Illustrative 3D portion", color = Color.White)
                            Button(onClick = { rotation = (rotation + 45f) % 360f }) { Text("Rotate dish") }
                        } else {
                            Button(onClick = { rotation = 0f; route = route.preview(models.containsKey(dish.model)) }, enabled = ready && models.containsKey(dish.model)) {
                                Text(if (!ready) "Loading portion…" else if (models.containsKey(dish.model)) "View portion" else "Portion unavailable")
                            }
                        }
                        Button(onClick = {
                            if (requestedModel != null) {
                                context.startActivity(Intent(context, GameActivity::class.java).addFlags(
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT))
                            } else route = route.back()
                        }) {
                            Text(if (requestedModel != null) "Back to game" else if (portion) "Back to dish" else "Back to night market")
                        }
                    }
                }
            }
        }
    )
}
