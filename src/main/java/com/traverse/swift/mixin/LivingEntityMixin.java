package com.traverse.swift.mixin;

import com.traverse.swift.Swift;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    private static final UUID SPEED_BOOST_UUID = UUID.fromString("cd48113e-57d2-4f06-a31c-d155cbde157a");

    @Inject(method = "jump", at = @At("TAIL"))
    public void onJump(CallbackInfo ci){
        LivingEntity player = (LivingEntity) (Object) this;
        ItemStack stack = player.getEquippedStack(EquipmentSlot.FEET);
        int level = getSwiftLevel(player);
        if(level > 0) {
            Vec3d vec3d = player.getVelocity();
            player.setVelocity(vec3d.x, vec3d.y + (0.1 * level), vec3d.z);
            if(Math.random() < Swift.CONFIG.damageChanceToEquipment){
                stack.damage(1, player, (p) -> p.sendEquipmentBreakStatus(EquipmentSlot.FEET));
            }
        }
    }
    
    @Inject(method = "tick", at = @At("HEAD"))
    public void onTick(CallbackInfo ci){
        LivingEntity player = (LivingEntity) (Object) this;
        var level = getSwiftLevel(player);
        player.setStepHeight(level > 0 ? 1.25f : 0.6f);

        EntityAttributeInstance speed = player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if(speed != null) {
            if(level > 0 && player.isOnGround() && !player.isSneaking()) {
                speed.removeModifier(SPEED_BOOST_UUID);
                speed.addPersistentModifier(new EntityAttributeModifier(SPEED_BOOST_UUID, "swift_movement", 0.02 * level, EntityAttributeModifier.Operation.ADDITION));
            }
            else {
                speed.removeModifier(SPEED_BOOST_UUID);
            }
        }
    }

    private int getSwiftLevel(LivingEntity player) {
        ItemStack stack = player.getEquippedStack(EquipmentSlot.FEET);
      return  EnchantmentHelper.getLevel(Swift.SWIFT_ENCHANTMENT, stack);
    }

}
