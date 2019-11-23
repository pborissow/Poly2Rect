# Poly2Rect
Used to approximate a polygon with rectangles - a process known as
rectangular decomposition. This algorithm starts by computing the largest
inscribed rectangle within a convex hull. Rectangles are generated around
the inscribed rectangle which provides the general shape of the polygon.


![Poly2Rect Example](https://raw.githubusercontent.com/pborissow/Poly2Rect/master/example.png)

For best results, this function can be called iteratively by clipping the
source polygon with rectangles generated by the getOverlappingRectangles()
method and feeding the clipped polygons into new instances of this class.

# Credits

- [Daniel Sud](http://cgm.cs.mcgill.ca/~athens/cs507/Projects/2003/DanielSud/) for the Inscribed Rectangle algorithm
- [Project Nayuki](https://www.nayuki.io/page/convex-hull-algorithm) for the Convex Hull implementation


# Possible Future Enhancements

Instead of relying on an inscribed rectangle, break up polygon into convex
regions using a library like [Bayazit](https://mpen.ca/406/bayazit)