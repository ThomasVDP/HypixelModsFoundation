# Minecraft Custom Server Mods (Forge 1.8.9)
This is a forge mod to automatically enable and disable mods on certain servers.

## Main idea
Don't you sometimes have that feeling of missing features on a server?
And wouldn't such feature developers want those features to only be active on certain servers?

This mod tries to answer those questions in two ways:

- `@SubscribeEvent`-s will only be called on the feature developer's server(s) of choice
- `ICommand`-s will only be registered on the feature developer's server(s) of choice

## Getting Started (for Forge users)
Download the latest [release](https://github.com/ThomasVDP/MinecraftServerModsFoundation/releases) and put the `CustomServerModsFoundation-*.*.jar`-file in your Minecraft mods directory!

Put all your extensions/mods made for this mod in `/path/to/minecraft/CustomServerMods/`!

That's it, the mod is start working instantly!

## Getting Started (for developers)
This mod's API can be added to your project using JitPack.
[![](https://jitpack.io/v/ThomasVDP/MinecraftServerModsFoundation.svg)](https://jitpack.io/#ThomasVDP/MinecraftServerModsFoundation)

Your `main instance` should implement the `ServerModBase` interface.
`ServerModBase#onRegisterEventSubscribers(IEventSubscribeRegister)` will be called to subscribe `@SubscribeEvent`-s.
`ServerModBase#onRegisterCommands(ICommandRegister)` will be called to register custom `ICommand`-s.

#### Info.json
The addon/mod requires a `info.json` at the root of the jar-file.

It contains name-value pairs to identify the mod:
```json
{
	"id": "your mods id",
	"name": "your mods display name",
	"description": "A description for you mod",
	"version": "your mod's version",
	"authorList": [ "all", "the", "authors" ],
	"credits": "possible credits",
	"guiFactory": "full name of you GuiFactory-class"
}
```

Everything is required except for the `"guiFactory"` pair.
This `"guiFactory"` name will be used to provide an in-game mod configuration screen in `Mod Options`


That's all you need to know!
Happy coding!

> Written with [StackEdit](https://stackedit.io/).
