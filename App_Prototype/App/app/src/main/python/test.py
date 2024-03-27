import numpy as np

def simple_sort(my_list):
    print("my_list in python fn: ", my_list)
    my_list = list(my_list)
    my_list.sort()
    return np.array(my_list)