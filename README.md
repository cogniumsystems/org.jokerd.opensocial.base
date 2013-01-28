org.jokerd.opensocial.base
==========================

Basic utilities used by other repositories:

https://github.com/cogniumsystems/org.jokerd.opensocial.base/tree/master/src/main/java/org/jokerd/opensocial/:

- The "cursors" package contains  classes used to manipulate with
"streams" of objects. These cursors allow to merge/order/re-group
objects comming from different sources. For example it is used to
represent a set of activity entries generated from multiple RSS feeds
as a single sequence of ordered entires.

- The "scheduler" package contains basic implementation of scheduler
used to periodically check and retrieve new entries. For example it is
used to check a twitter account for new messages once in 2 minutes and
load a list of feeds once at 5 min. Schedulers work as "deamons"  used
to automatically retreive and feed new content in a local cache. One
of possible implementations of such a cache is based on a "store" (see
the next project).
