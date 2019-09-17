from typing import List, Callable, TypeVar, Generic


T = TypeVar('T')
T2 = TypeVar('T2')


class ExtList(Generic[T]):
    def __init__(self, col: List[T]):
        self.extList: List[T] = col

    def map(self, function: Callable[[T], T2]) -> List[T2]:
        return list(map(function, self.extList))

    def mapE(self, function: Callable[[T], T2]) -> 'ExtList':
        return ExtList(self.map(function))

    def foreach(self, function: Callable[[T], None]) -> None:
        map(self, function)

    def get(self) -> List[T]:
        return self.extList

    def filter(self, function: Callable[[T], bool]) -> List[T]:
        result = []
        for e in self.extList:
            if function(e) is True:
                result.append(e)
        return result

    def filterE(self, function: Callable[[T], bool]) -> 'ExtList':
        return ExtList(self.filter(function))

    def fold(self, initial: T2, func: Callable[[T2, T], T2]) -> T2:
        for e in self.extList:
            initial = func(initial, e)
        return initial
