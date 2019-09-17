import libs.ExtList as ExtList
import unittest


class ExtListTestCase(unittest.TestCase):

    def testMap(self):
        list1: ExtList[int] = ExtList.ExtList([1, 2, 3])
        list2 = ExtList.ExtList(["2", "3", "4"])
        self.assertEqual(list1.map(lambda e: str(e+1)), list2.get())

    def testFilter(self):
        list1: ExtList.ExtList[int] = ExtList.ExtList[int]([1, 2, 3, 4, 5])
        self.assertEqual(list1.filter(lambda e: e > 2), [3, 4, 5])

    def testFold(self):
        list1 = ExtList.ExtList([1, 2, 3, 4])
        sum = list1.fold(0, lambda acc, e: acc + e)
        self.assertEqual(sum, 1+2+3+4)


if __name__ == '__main__':
    unittest.main()
