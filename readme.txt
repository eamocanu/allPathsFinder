

Rough outline on what to touch:
Improvements time from days to seconds.

2 heuristics which made a huge diff are:

#1. Checking if path creates a disconnected graph-if it does, then return to prev recursive step (this is currently done through BFS, and it could be improved a bit)
#2. If path creates a bridge in the graph (have 2 graph components connected by a bridge) return in some cases
	#2.1. A minor speed up (related to #2 above)
		-if vertex is of degree 1 (like a cusp), it must be visited first before going into its other adjacent neighbours. If it did go to the neighbours, it could never ever go back and visit the 1 degree vertex so that leads to an incomplete path



I sped up #1, by eliminating BFS and keeping track of the last bridge node found
	Record last time path goes in an edge room. If crtroom is edge and there was another edge visited (&& the 2 edge rooms are not adjacent) then we might have disconnected components since path went from 1 edge to another. So to be sure we need make sure that around this crt room edge there are at least 2 adjacent free cells/or around that prev edge room.


Some minor improvements are still possible: check for 2-connectedness (going off #2 above). If you go visit a part of the graph using one "way" you must have a "second" way to come back to the way w the end room. So these 2 ways are like 2 adjacent bridges in the graph. 


Here are some parts of my log which I wrote as I went and improved my algo. I should call this section "From Days to Seconds"

7 8
2 0 0 0 0 0 0
0 0 0 0 0 0 0
0 0 0 0 0 0 0
0 0 0 0 0 0 0
0 0 0 0 0 0 0
0 0 0 0 0 0 0
0 0 0 0 0 0 0
3 0 0 0 0 1 1

Raw (no improvements):
3 days... :(

w v2:
Total solutions: 301716
Total steps ff: 37,921,431
exec time: 11942469ms or 3.3 hrs

w v3:
total solutions: 301716
Total steps ff: 7580964
exec time: 2088797ms (34.8mins)

w v3 w edge only modification: NOT WORTH IT
Total solutions: 301716
Total steps: 9645357
exec time: 2048859ms (34.1 mins)
More branching but easier to check them so less time (by 30 secs lol)

W V3 BUT w bridge before connected:
Total solutions: 301716
Total steps: 7580964
exec time: 1909719ms (32mins)

w v3 bB4Connected check AND deg1 check (slight reduction of 10mins:))
Total solutions: 301716
Total steps: 7207363
exec time: 1337750ms (22 minutes) 
w firefox closed:975594ms (16.3 mins)

w threads: (it was worth a try haha)
Total solutions: 301716
Total steps: 7207363
exec time: 1001156ms (16.7 mins) no Firefox)

Total solutions: 301716
Total steps: 7129500
exec time: 1085656ms (18 mins) but less branches
	   1121328ms (18.7mins) but W/ branches

on a 2.8GHz on linux it took
Total solutions: 301716
Total steps: 7207363
exec time: 15966ms or 16 seconds

The overhead from checking for connectedness must be less than the overhead of going into bad territories and doing recursion there.
Check v2 more than halves the number of checks w/o adding any extra overhead for checking.

