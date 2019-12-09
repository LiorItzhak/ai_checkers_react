package Utills


abstract class Observable<T> {
    private var isValueInitialized = false
    open var value: T? = null
        protected set(value) {
            isValueInitialized = true
            field = value
            console.info("4-------------------------------------------------df----------")

            notifyObservers()
        }

    private val observers = mutableSetOf<Observer<T>>()

     fun observe(observer: Observer<T>) {
         console.info("observe-${observers.size}-----------------------------------------------df----------")

         observers.add(observer)
//        if (isValueInitialized)
//            observer.onChanged(value!!)
    }

    private fun notifyObservers() {
        //todo - notify on different coroutines
        console.info("Obs observers size =-${observers.size}-------------------------------df----------")
        if (isValueInitialized)
            observers.forEach {
                console.info("Obs notify value------------------------------------------------df----------")
                it.onChanged(value!!)
            }
    }

     fun removeObserver(observer: Observer<T>) {
        observers.remove(observer)
    }

    fun <R> map(mappingFun: (T)->R): Observable<R> {
        val obs =  object : Observable<R>(){}
        observeForever {
            obs.value = mappingFun.invoke(it)
        }
        return obs
    }
}

class MutableObservable<T>() : Observable<T>() {
    constructor(initializedValue: T) : this() {
        value = initializedValue
    }

    override var value:T?
    public set(value) {
        console.info("Obs Set value------------------------------------------------df----------")
        super.value = value
    }
    get() = super.value
}


abstract class Observer<T> {
    abstract fun onChanged(value: T)
}


inline fun <T> Observable<T>.observeForever(crossinline onChanged: (T) -> Unit) {
    console.info("observeForever-----------------------------------------------df----------")
    observe(object : Observer<T>() {
        override fun onChanged(value: T) {
            onChanged.invoke(value)
        }
    })
}

