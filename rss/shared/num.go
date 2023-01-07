package shared

type number interface {
	~int | ~int8 | ~int16 | ~int32 | ~int64 |
		~uint | ~uint8 | ~uint16 | ~uint32 | ~uint64 | ~uintptr |
		~float32 | ~float64
}

func Max[T number](x, y T) T {
	if x < y {
		return y
	}
	return x
}

func Min[T number](x, y T) T {
	if x > y {
		return y
	}
	return x
}
