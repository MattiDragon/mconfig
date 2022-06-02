# Mconfig
Mconfig is a simple configuration api made for MattiDragons mods. 

**WARNING: mconfig 1.x for minecraft 1.19 is only temporarily supported. It will be replaced by 
mconfig 2.x in the future**

## Usage
Get it with jitpack.

You can register your configs with `ConfigManager#register`. It will return you a `Config` 
object that can be used to read the value of the config. The default value will be used to get 
the config class. Only records are supported. 
Their components can be primitives, their wrappers, strings or enums. Other types may be added 
later.

You can force a reload of your config by using `Config#reload` or you can reload client and 
server configs with commands. Note that no configs are synced from the server to the client and 
that nothing is stopping code from reloading common configs.