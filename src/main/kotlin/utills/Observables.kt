package utills


abstract class Observable<T> {
    private var dataIsSet = false


    private val observers = mutableSetOf<Observer<T>>()

    fun observe(observer: Observer<T>) {
        observers.add(observer)
        if (dataIsSet)
            observer.onChanged(data as T)
    }

    private fun notifyObservers() {
        //todo - notify on different coroutines
        if (dataIsSet)
            observers.forEach { it.onChanged(data as T) }
    }

    fun removeObserver(observer: Observer<T>) {
        observers.remove(observer)
    }

    fun <R> map(mappingFun: (T) -> R): Observable<R> {
        val obs = object : Observable<R>() {}
        observeForever { obs.value = mappingFun.invoke(it) }
        return obs
    }


    private var data: T?=null

    open var value : T
        get() = data as T
    protected set(value) {
        dataIsSet = true
        this.data = value
        notifyObservers()
    }

}

class MutableObservable<T>() : Observable<T>() {
    constructor(initializedValue: T) : this() {
        value = (initializedValue)
    }

    override var value: T
        get() = super.value
        public set(value) {super.value = value}
}


abstract class Observer<T> {
    abstract fun onChanged(value: T)
}


inline fun <T> Observable<T>.observeForever(crossinline onChanged: (T) -> Unit) {
    observe(object : Observer<T>() {
        override fun onChanged(value: T) {
            onChanged.invoke(value)
        }
    })
}

