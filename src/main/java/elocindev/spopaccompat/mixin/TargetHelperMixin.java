package elocindev.spopaccompat.mixin;

import net.spell_engine.utils.TargetHelper;
import net.spell_engine.utils.TargetHelper.Relation;
import xaero.pac.common.server.api.OpenPACServerAPI;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TargetHelper.class)
public class TargetHelperMixin {
	@Inject(at = @At("HEAD"), method = "allowedToHurt", cancellable = true)
	private static void spellengineopacsupport$damageOpacLogic(Entity e1, Entity e2, CallbackInfoReturnable<Boolean> cir) {
		if (!checkOpac(e1, e2)) {
			cir.setReturnValue(false);
		}
	}

	@Inject(at = @At("HEAD"), method = "getRelation", cancellable = true)
	private static void spellengineopacsupport$relationOpacLogic(LivingEntity attacker, Entity target, CallbackInfoReturnable<Relation> cir) {
		// Spell Engine 1.0+ changes Relation.FRIENDLY to Relation.ALLY.
		// Friendly will still work due to allowedToHurt logic for now.
		// So as long as that does not change, this should work for 1.19 to 1.21.
		if (attacker == target || !checkOpac(attacker, target)) cir.setReturnValue(Relation.FRIENDLY);
	}

	private static boolean checkOpac(Entity attackerEntity, Entity targetEntity) {
		if (!(attackerEntity instanceof Player player) || player.level().isClientSide() || !(targetEntity instanceof Player target)) {
			return true;
		}
	
		MinecraftServer server = player.getServer();
		if (server == null) {
			return true;
		}
	
		UUID playerUUID = player.getUUID();
		UUID targetUUID = target.getUUID();
	
		try {
			OpenPACServerAPI api = OpenPACServerAPI.get(server);
			var partyManager = api.getPartyManager();
	
			var playerParty = partyManager.getPartyByMember(playerUUID);
			var targetParty = partyManager.getPartyByMember(targetUUID);
	
			if (playerParty != null && targetParty != null) {
				UUID playerPartyId = playerParty.getId();
				UUID targetPartyId = targetParty.getId();
	
				if (!playerPartyId.equals(targetPartyId)) {
					return !playerParty.isAlly(targetPartyId);
				}
			}
	
			if (playerParty != null) {
				List<ServerPlayer> memberList = playerParty.getOnlineMemberStream().toList();
				if (!memberList.isEmpty() && target instanceof ServerPlayer serverTarget) {
					return !memberList.contains(serverTarget);
				}
			}
		} catch (Exception e) {}
	
		return true;
	}
}