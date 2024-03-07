package ru.nsk.kstatemachine.event

import ru.nsk.kstatemachine.state.*
import ru.nsk.kstatemachine.statemachine.StateMachine
import ru.nsk.kstatemachine.statemachine.processEventBlocking

/**
 * Base interface for events which may trigger transitions of [StateMachine]
 */
interface Event

/**
 * Event holding some data
 */
interface DataEvent<out D : Any> : Event {
    val data: D
}

/**
 * User may call [StateMachine.processEventBlocking] with [UndoEvent] as alternative to calling machine.undo()
 */
object UndoEvent : Event

/**
 * Marker interface for all events that are generated by the library itself
 */
sealed interface GeneratedEvent : Event

/**
 * Special event generated by the library when a state is finished.
 * Transitions use special event matcher by default to match only related events.
 * If [FinishedEvent] is generated by [FinalDataState] entry, [data] field of event will receive data
 * from this state using [DataExtractor]
 */
class FinishedEvent internal constructor(val state: IState, val data: Any? = null) : GeneratedEvent

/**
 * Initial event which is processed on state machine start
 */
sealed interface StartEvent : GeneratedEvent {
    val startState: IState
}

/**
 * [startStates] must contain at least one state. If there are multiple states they must be sub-children of a parallel
 * state.
 */
internal class StartEventImpl(val startStates: Set<IState>) : StartEvent {
    override val startState = startStates.first()
}

internal class StartDataEventImpl<D : Any>(
    override val startState: DataState<D>,
    override val data: D
) : StartEvent, DataEvent<D>

internal object StopEvent : GeneratedEvent

/**
 * This event is processed even is the [StateMachine] already stopped
 */
internal class DestroyEvent(val stop: Boolean) : GeneratedEvent

/**
 * System event which is used by the library to wrap original event and argument,
 * so user may access them, when this event is processed.
 * Currently only [UndoEvent] is transformed to this event.
 * @param event original event
 * @param argument original argument
 */
class WrappedEvent(val event: Event, val argument: Any?) :
    Event