package com.creativemd.littletiles.common.packet;

import java.util.Iterator;
import java.util.UUID;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.utils.rotation.DoorTransformation;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

public class LittleEntityRequestPacket extends CreativeCorePacket {
	
	public LittleEntityRequestPacket() {
		
	}
	
	public UUID uuid;
	public NBTTagCompound nbt;
	public boolean completeData;
	
	public LittleEntityRequestPacket(UUID uuid, NBTTagCompound nbt, boolean completeData) {
		this.uuid = uuid;
		this.nbt = nbt;
		this.completeData = completeData;
	}

	@Override
	public void writeBytes(ByteBuf buf) {
		writeString(buf, uuid.toString());
		buf.writeBoolean(completeData);
		writeNBT(buf, nbt);
	}

	@Override
	public void readBytes(ByteBuf buf) {
		uuid = UUID.fromString(readString(buf));
		completeData = buf.readBoolean();
		nbt = readNBT(buf);
	}

	@Override
	public void executeClient(EntityPlayer player) {
		EntityAnimation animation = null;
		for (Iterator<Entity> iterator = player.worldObj.getLoadedEntityList().iterator(); iterator.hasNext();) {
			Entity entity = iterator.next();
			if(entity instanceof EntityAnimation && entity.getUniqueID().equals(uuid))
			{
				animation = (EntityAnimation) entity;
				break;
			}
		}
		
		if(animation != null)
		{
			if(completeData)
			{
				animation.readFromNBT(nbt);
				animation.approved = true;
			}else{
				DoorTransformation transformation = DoorTransformation.loadFromNBT(nbt);
				animation.approved = animation.transformation.equals(transformation);					
			}
			
		}
	}

	@Override
	public void executeServer(EntityPlayer player) {
		EntityAnimation animation = null;
		for (Iterator<Entity> iterator = player.worldObj.getLoadedEntityList().iterator(); iterator.hasNext();) {
			Entity entity = iterator.next();
			if(entity instanceof EntityAnimation && entity.getUniqueID().equals(uuid))
			{
				animation = (EntityAnimation) entity;
				break;
			}
		}
		
		if(animation != null)
		{
			if(completeData)
				PacketHandler.sendPacketToPlayer(new LittleEntityRequestPacket(uuid, animation.writeToNBT(new NBTTagCompound()), completeData), (EntityPlayerMP) player);
			else{
				PacketHandler.sendPacketToPlayer(new LittleEntityRequestPacket(uuid,  animation.transformation.writeToNBT(new NBTTagCompound()), completeData), (EntityPlayerMP) player);
			}
		}
	}

	
	
}